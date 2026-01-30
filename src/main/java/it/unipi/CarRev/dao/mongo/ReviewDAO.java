package it.unipi.CarRev.dao.mongo;

import it.unipi.CarRev.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReviewDAO extends MongoRepository<Review,String> {
}
