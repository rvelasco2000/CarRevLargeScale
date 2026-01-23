package it.unipi.CarRev.dao.mongo;

import it.unipi.CarRev.model.UserBasedAnalytics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBasedAnalyticsDAO extends MongoRepository<UserBasedAnalytics,String> {
}
