package it.unipi.CarRev.service.Impl;

import it.unipi.CarRev.dao.mongo.SaleDAO;
import it.unipi.CarRev.model.Car;
import it.unipi.CarRev.model.Sale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductYearRecomputeService {

    private static final Logger logger = LoggerFactory.getLogger(ProductYearRecomputeService.class);

    private final MongoTemplate mongoTemplate;
    private final SaleDAO saleDAO;

    public ProductYearRecomputeService(MongoTemplate mongoTemplate, SaleDAO saleDAO) {
        this.mongoTemplate = mongoTemplate;
        this.saleDAO = saleDAO;
    }

    @Scheduled(cron = "${car.sales.recompute-cron:0 0 0 * * MON}")
    public void scheduledRecompute() {
        int updated = recomputeAllCars();
        logger.info("Scheduled productYear recompute completed; updatedCars={}", updated);
    }

    public int recomputeAllCars() {
        List<Car> cars = mongoTemplate.findAll(Car.class);
        if (cars.isEmpty()) {
            return 0;
        }

        BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Car.class);
        int updatedCars = 0;

        for (Car car : cars) {
            List<String> saleIds = car.getSales();
            if (saleIds == null || saleIds.isEmpty()) {
                continue;
            }

            List<Sale> sales = saleDAO.findAllById(saleIds);
            if (sales.isEmpty()) {
                continue;
            }

            Map<Integer, Aggregate> aggregates = new HashMap<>();
            for (Sale sale : sales) {
                Integer year = sale.getYear();
                Integer mileage = sale.getMileage();
                Double price = sale.getPrice();
                if (year == null || mileage == null || price == null) {
                    continue;
                }
                Aggregate agg = aggregates.computeIfAbsent(year, y -> new Aggregate());
                agg.count++;
                agg.totalMileage += mileage;
                agg.totalPrice += price;
            }

            if (aggregates.isEmpty()) {
                continue;
            }

            List<Map<String, Object>> productYearList = new ArrayList<>(aggregates.size());
            aggregates.entrySet().stream()
                    .sorted(Comparator.comparingInt(Map.Entry::getKey))
                    .forEach(entry -> {
                        int year = entry.getKey();
                        Aggregate agg = entry.getValue();
                        double avgMileage = agg.totalMileage / (double) agg.count;
                        double avgPrice = agg.totalPrice / agg.count;
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("Year", year);
                        row.put("Average_used_milage", (int) Math.round(avgMileage));
                        row.put("Average_used_price", avgPrice);
                        row.put("Sales_count", agg.count);
                        productYearList.add(row);
                    });

            Query query = new Query(Criteria.where("id").is(car.getId()));
            Update update = new Update().set("productYear", productYearList);
            bulkOperations.updateOne(query, update);
            updatedCars++;
        }

        if (updatedCars > 0) {
            bulkOperations.execute();
        }

        return updatedCars;
    }

    private static final class Aggregate {
        private int count = 0;
        private long totalMileage = 0;
        private double totalPrice = 0.0;
    }
}
