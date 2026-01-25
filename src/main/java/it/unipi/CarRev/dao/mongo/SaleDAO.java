package it.unipi.CarRev.dao.mongo;

import it.unipi.CarRev.model.Sale;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SaleDAO extends MongoRepository<Sale, String> {
}
