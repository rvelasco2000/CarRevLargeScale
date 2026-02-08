package it.unipi.CarRev.dao.mongo;

import it.unipi.CarRev.model.CarAnalytics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarAnalyticsDAO extends MongoRepository<CarAnalytics, String> {
    Optional<CarAnalytics> findByData(String data);
}
