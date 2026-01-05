package it.unipi.CarRev.dao.mongo;

import it.unipi.CarRev.model.TrafficForAnalytics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrafficForAnalyticsDAO extends MongoRepository<TrafficForAnalytics,String>{
}
