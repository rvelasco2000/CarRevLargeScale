package it.unipi.CarRev.service.Impl;


import it.unipi.CarRev.dao.mongo.CarDAO;
import it.unipi.CarRev.dto.CarCreateRequestDTO;
import it.unipi.CarRev.model.Car;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class InsertNewCarServiceImpl{
    private final MongoTemplate mongoTemplate;
    public InsertNewCarServiceImpl(MongoTemplate mongoTemplate){
        this.mongoTemplate=mongoTemplate;
    }
    public void insertCar(CarCreateRequestDTO car){
        Car newCar=new Car();
    }

}
