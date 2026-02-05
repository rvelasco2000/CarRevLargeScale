package it.unipi.CarRev.service.Impl;


import it.unipi.CarRev.dao.mongo.CarDAO;
import it.unipi.CarRev.dto.CarCreateRequestDTO;
import it.unipi.CarRev.model.Car;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import it.unipi.CarRev.service.Neo4jCarInsertService;

import java.util.ArrayList;
import java.util.List;

@Service
public class InsertNewCarServiceImpl{

    private final Neo4jCarInsertService neo4jService;
    private final CarDAO carDAO;

    public InsertNewCarServiceImpl(CarDAO carDAO,Neo4jCarInsertService neo4jService){

        this.carDAO = carDAO;
        this.neo4jService = neo4jService;
    }
    public Boolean insertCar(CarCreateRequestDTO car){
        Car newCar=new Car(
                car.getCarName(),
                car.getCarBrand(),
                car.getCarModel(),
                car.getBodyType(),
                car.getDriveWheels(),
                car.getEngineDisplacement(),
                car.getNumberOfCylinders(),
                car.getTransmissionType(),
                car.getHorsePower(),
                car.getFuelType(),
                car.getSeatCapacity(),
                car.getPriceNew(),
                0.0,
                new ArrayList<Document>(),
                new ArrayList<ObjectId>(),
                new ArrayList<String>(),
                0L,
                new ArrayList<>(),
                car.getProductionYear(),
                0,
                0

        );
        try{
            carDAO.save(newCar);
            //neo4jService.insertCar(car); // Neo4j
            return true;
        }
        catch(Exception e){
            System.out.println("an error has occurred while inserting a car:"+e.getMessage());
            return false;
        }

    }

}

