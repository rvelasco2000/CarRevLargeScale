package it.unipi.CarRev.service.Impl;


import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import it.unipi.CarRev.config.RedisConfig;
import it.unipi.CarRev.dao.mongo.CarDAO;
import it.unipi.CarRev.model.Car;
import it.unipi.CarRev.model.Review;
import it.unipi.CarRev.model.User;
import it.unipi.CarRev.service.exception.ResourceNotFoundException;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import it.unipi.CarRev.service.Neo4jCarDeleteService;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

@Service
public class DeleteACarServiceImpl {
    private final CarDAO carDAO;
    private final MongoTemplate mongoTemplate;
    private final Neo4jCarDeleteService neo4jCarDeleteService ;
    public DeleteACarServiceImpl(CarDAO carDAO,Neo4jCarDeleteService neo4jCarDeleteService, MongoTemplate mongoTemplate){
        this.carDAO=carDAO;
        this.neo4jCarDeleteService = neo4jCarDeleteService;
        this.mongoTemplate=mongoTemplate;
    }

    @Transactional("mongoTransactionManager")
    public void deleteCar(String id){
        try {
            /*
            if(!carDAO.existsById(id)){
                System.out.println("car not found");
                return -1;
            }
            */
            Car oldCar = carDAO.findById(id).orElse(null);
            if (oldCar == null) {
                //System.out.println("car not found (race condition)");
                //return -1;
                throw new ResourceNotFoundException("this car do not exists in the dataset");
            }
            /*
            int deleted = neo4jCarDeleteService.deleteCarProjection(oldCar.getId());
            if (deleted != 1) {
                System.out.println("neo4j mismatch on delete: deleted=" + deleted);
                return -2; //
            }
            carDAO.deleteById(id);

             */
            //int result=deleteInRedis(id);
            deleteCar(oldCar);
           // return 0;

        }
        catch(Exception e){

            System.out.println("an error has occurred during the delete of a car");
            e.printStackTrace();
            throw new RuntimeException("an error has occurred during the delete of a car");
            //return -2;
        }
    }
    private void deleteCar(Car oldCar){
        List<ObjectId> carReviewsId=getAllId(oldCar);
        if(carReviewsId.isEmpty()){
            carDAO.deleteById(oldCar.getId());
            System.out.println("user correctly deleted");
            return;
        }
        String deletedCarName="Deleted Car";
        Query reviewQuery=new Query(Criteria.where("_id").in(carReviewsId));
        Update reviewUpdate=new Update().set("car_name",deletedCarName);
        mongoTemplate.updateMulti(reviewQuery,reviewUpdate, Review.class);

        Query userQuery=new Query(Criteria.where("reviews._id").in(carReviewsId));
        UpdateOptions userUpdateOptions= new UpdateOptions().arrayFilters(List.of(
                Filters.in("idRev._id",carReviewsId)
        ));
        Update userUpdate=new Update().set("reviews.$[idRev].car_name",deletedCarName);
        mongoTemplate.getCollection("users").updateMany(
                userQuery.getQueryObject(),
                userUpdate.getUpdateObject(),
                userUpdateOptions
        );
        carDAO.deleteById(oldCar.getId());
        System.out.println("user correctly deleted");

    }
    private List<ObjectId> getAllId(Car car){
        List<ObjectId> idList=new ArrayList<>();
        if(car.getTopTenReview()!=null){
            for(Document review: car.getTopTenReview()){
                idList.add(review.getObjectId("_id"));
            }
        }
        if(car.getOtherReview()!=null){
            for(Document review: car.getOtherReview()){
                idList.add(review.getObjectId("_id"));
            }
        }
        return idList;
    }
    /*
    private int deleteInRedis(String id){
        final String CARKEY="Car:"+id+":views";
        try(Jedis jedis= RedisConfig.getJedis()){
            jedis.del(CARKEY);
            return 0;
        }
        catch(Exception e){
            System.out.print("error during the delete of cached views of a car in redis");
            return -2;
        }


    }

     */
}

