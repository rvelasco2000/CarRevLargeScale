package it.unipi.CarRev.service.Impl;

import it.unipi.CarRev.dto.CarSearchResponse;
import it.unipi.CarRev.dto.CarSummaryDTO;
import it.unipi.CarRev.model.Car;
import it.unipi.CarRev.service.CarSearchService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class CarSearchServiceImpl implements CarSearchService {

    private final MongoTemplate mongoTemplate;
    private final StringRedisTemplate redisTemplate;
    private final Duration countTtl;

    public CarSearchServiceImpl(MongoTemplate mongoTemplate,
                                StringRedisTemplate redisTemplate,
                                @Value("${carsearch.count-cache-ttl-seconds:60}") long countTtlSeconds) {
        this.mongoTemplate = mongoTemplate;
        this.redisTemplate = redisTemplate;
        this.countTtl = Duration.ofSeconds(countTtlSeconds);
    }

    @Override
    public CarSearchResponse search(String carName,
                                    String carBrand,
                                    String carModel,
                                    String bodyType,
                                    Integer engineDisplacement,
                                    Integer numberOfCylinders,
                                    int page,
                                    int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int skip = (safePage - 1) * safeSize;

        List<Criteria> criteriaList = new ArrayList<>();

        if (carName != null && !carName.isBlank()) {
            criteriaList.add(Criteria.where("car_name")
                    .regex(Pattern.compile(Pattern.quote(carName), Pattern.CASE_INSENSITIVE)));
        }
        if (carBrand != null && !carBrand.isBlank()) {
            criteriaList.add(Criteria.where("car_brand")
                    .regex(Pattern.compile(Pattern.quote(carBrand), Pattern.CASE_INSENSITIVE)));
        }
        if (carModel != null && !carModel.isBlank()) {
            criteriaList.add(Criteria.where("car_model")
                    .regex(Pattern.compile(Pattern.quote(carModel), Pattern.CASE_INSENSITIVE)));
        }
        if (bodyType != null && !bodyType.isBlank()) {
            criteriaList.add(Criteria.where("body_type")
                    .regex(Pattern.compile(Pattern.quote(bodyType), Pattern.CASE_INSENSITIVE)));
        }
        if (engineDisplacement != null) {
            criteriaList.add(Criteria.where("engine_displacement").is(engineDisplacement));
        }
        if (numberOfCylinders != null) {
            criteriaList.add(Criteria.where("number_of_cylinders").is(numberOfCylinders));
        }

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long total = getCachedCount(
                carName,
                carBrand,
                carModel,
                bodyType,
                engineDisplacement,
                numberOfCylinders,
                query
        );

        query.with(Sort.by(Sort.Direction.DESC, "views"));
        query.skip(skip).limit(safeSize);

        List<Car> cars = mongoTemplate.find(query, Car.class);
        List<CarSummaryDTO> items = new ArrayList<>(cars.size());
        for (Car car : cars) {
            items.add(new CarSummaryDTO(
                    car.getId(),
                    car.getCarName(),
                    car.getCarBrand(),
                    car.getCarModel(),
                    car.getBodyType(),
                    car.getEngineDisplacement(),
                    car.getNumberOfCylinders(),
                    car.getViews()
            ));
        }

        int totalPages = (int) Math.ceil((double) total / safeSize);
        return new CarSearchResponse(items, safePage, safeSize, total, totalPages);
    }

    private long getCachedCount(String carName,
                                String carBrand,
                                String carModel,
                                String bodyType,
                                Integer engineDisplacement,
                                Integer numberOfCylinders,
                                Query query) {
        String key = buildCountCacheKey(
                carName,
                carBrand,
                carModel,
                bodyType,
                engineDisplacement,
                numberOfCylinders
        );
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return Long.parseLong(cached);
            } catch (NumberFormatException ignored) {
                // Fall through to recompute when cache is corrupted.
            }
        }
        long total = mongoTemplate.count(query, Car.class);
        redisTemplate.opsForValue().set(key, Long.toString(total), countTtl);
        return total;
    }

    private String buildCountCacheKey(String carName,
                                      String carBrand,
                                      String carModel,
                                      String bodyType,
                                      Integer engineDisplacement,
                                      Integer numberOfCylinders) {
        String signature = "carName=" + normalize(carName)
                + "|carBrand=" + normalize(carBrand)
                + "|carModel=" + normalize(carModel)
                + "|bodyType=" + normalize(bodyType)
                + "|engineDisplacement=" + normalize(engineDisplacement)
                + "|numberOfCylinders=" + normalize(numberOfCylinders);
        return "carsearch:count:" + sha256Base64(signature);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? "" : trimmed.toLowerCase();
    }

    private String normalize(Integer value) {
        return value == null ? "" : value.toString();
    }

    private String sha256Base64(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception ex) {
            return Integer.toHexString(input.hashCode());
        }
    }
}
