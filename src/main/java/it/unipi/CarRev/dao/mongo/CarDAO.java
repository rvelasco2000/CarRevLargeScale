package it.unipi.CarRev.dao.mongo;

import it.unipi.CarRev.dao.mongo.projection.CarName;
import it.unipi.CarRev.dao.mongo.projection.CarRevAndOtherReviews;
import it.unipi.CarRev.model.Car;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CarDAO extends MongoRepository<Car,String> {
    CarName findCarById(String Id);
    CarRevAndOtherReviews findReviewsById(String id);
}
