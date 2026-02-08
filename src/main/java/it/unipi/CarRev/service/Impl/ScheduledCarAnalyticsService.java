package it.unipi.CarRev.service.Impl;

import it.unipi.CarRev.dao.mongo.CarAnalyticsDAO;
import it.unipi.CarRev.model.Car;
import it.unipi.CarRev.model.CarAnalytics;
import it.unipi.CarRev.utils.UtilsForDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduledCarAnalyticsService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledCarAnalyticsService.class);

    private final MongoTemplate mongoTemplate;
    private final CarAnalyticsDAO carAnalyticsDAO;

    public ScheduledCarAnalyticsService(MongoTemplate mongoTemplate, CarAnalyticsDAO carAnalyticsDAO) {
        this.mongoTemplate = mongoTemplate;
        this.carAnalyticsDAO = carAnalyticsDAO;
    }

    @Scheduled(cron = "${car.analytics.cron:0 5 0 * * *}")
    public void dailyCarAnalyticsSnapshot() {
        String todayDate = UtilsForDate.getDate();

        Query query = new Query();
        query.fields().include("car_name").include("car_brand").include("car_model")
                .include("views").include("number_of_reviews");

        List<Car> cars = mongoTemplate.find(query, Car.class);
        List<CarAnalytics.CarInfo> carsInfo = new ArrayList<>(cars.size());

        for (Car car : cars) {
            String carName = car.getCarName();
            String carBrand = car.getCarBrand();
            String carModel = car.getCarModel();
            String carId = car.getId();
            Long views = car.getViews() == null ? 0L : car.getViews();
            Integer numReviews = car.getNumberOfReviews() == null ? 0 : car.getNumberOfReviews();
            carsInfo.add(new CarAnalytics.CarInfo(carName, carBrand, carModel, carId, views, numReviews));
        }

        Optional<CarAnalytics> existing = carAnalyticsDAO.findByData(todayDate);
        if (existing.isPresent()) {
            CarAnalytics doc = existing.get();
            doc.setCarsInfo(carsInfo);
            carAnalyticsDAO.save(doc);
        } else {
            carAnalyticsDAO.save(new CarAnalytics(todayDate, carsInfo));
        }

        logger.info("Car analytics snapshot saved for date {}", todayDate);
    }
}
