package it.unipi.CarRev.service.Impl;

import com.mongodb.client.result.UpdateResult;
import it.unipi.CarRev.dao.mongo.CarDAO;
import it.unipi.CarRev.dao.mongo.ReviewDAO;
import it.unipi.CarRev.dao.mongo.UserDAO;
import it.unipi.CarRev.dao.mongo.projection.CarName;
import it.unipi.CarRev.dto.InsertReviewRequestDTO;
import it.unipi.CarRev.mapper.ReviewMapper;
import it.unipi.CarRev.model.Review;
import it.unipi.CarRev.service.exception.ResourceNotFoundException;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Service
public class WriteReviewServiceImpl {
    private final UserDAO userDAO;
    private final CarDAO carDAO;
    private final MongoTemplate mongoTemplate;
    private final ReviewDAO reviewDAO;
    private final ReviewMapper reviewMapper;
    public WriteReviewServiceImpl(UserDAO userDAO, CarDAO carDAO, MongoTemplate mongoTemplate, ReviewDAO reviewDAO, ReviewMapper reviewMapper){
        this.userDAO=userDAO;
        this.carDAO=carDAO;
        this.mongoTemplate=mongoTemplate;
        this.reviewDAO=reviewDAO;
        this.reviewMapper=reviewMapper;
    }
    @Transactional("mongoTransactionManager")
    public Boolean writeReview(InsertReviewRequestDTO insertReviewRequestDTO){
        System.out.println("Is transaction active? " + org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive());
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        String username=null;
        //this check should be handled by the security config but for good measure i will keep it
        if(auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            username = auth.getName();
            System.out.println("username="+username);
        }
        else{
            return false;
        }
        CarName carName=carDAO.findCarById(insertReviewRequestDTO.getCarId());
        if(carName==null){
            throw new ResourceNotFoundException("cannot find car with this id");
        }
        Document newReview=returnDocumentFromDTO(insertReviewRequestDTO,carName);
        Review review=reviewMapper.mapDocumentToReview(newReview,username);
        System.out.println(review.getText());
        newReview.remove("report");
        insertIntoUserCollection(newReview,username);
        reviewDAO.save(review);
        System.out.println("the review has been correctly saved in reviews collection");
        insertIntoCarCollection(newReview,insertReviewRequestDTO.getCarId(),username);
        return true;
    }
    private Document returnDocumentFromDTO(InsertReviewRequestDTO insertReviewRequestDTO,CarName carName){
        Document newReview=new Document()
                .append("_id",new ObjectId())
                .append("car_name",carName.getcarName())
                .append("text",insertReviewRequestDTO.getText())
                .append("rating",insertReviewRequestDTO.getRating())
                .append("timestamp", LocalDateTime.now())
                .append("likes",0)
                .append("report",0);
        if(insertReviewRequestDTO.getYear()!=null){
            newReview.append("year",insertReviewRequestDTO.getYear());
        }
        if(insertReviewRequestDTO.getMileage()!=null){
            newReview.append("mileage",insertReviewRequestDTO.getMileage());
        }
        return newReview;
    }
    private void insertIntoUserCollection(Document newReview,String username){
        Query query=new Query(Criteria.where("username").is(username));
        List<Document> pipeline=Arrays.asList(
                new Document("$set",new Document("reviews",
                        new Document("$concatArrays",Arrays.asList(
                                Collections.singletonList(newReview),
                                new Document("$ifNull",Arrays.asList("$reviews",Arrays.asList()))

                        ))
                )),
                new Document("$set",new Document("otherReviews",
                        new Document("$concatArrays",Arrays.asList(
                                new Document("$ifNull",Arrays.asList("$otherReviews",Arrays.asList())),
                                new Document("$cond",Arrays.asList(
                                        new Document("$gt",Arrays.asList(new Document("$size","$reviews"),10)),
                                        Arrays.asList(new Document("$arrayElemAt",Arrays.asList("$reviews._id",10))),
                                        Arrays.asList()
                                ))

                        )))),
                new Document("$set",new Document("reviews",
                        new Document("$slice",Arrays.asList("$reviews",10))))
        );

       UpdateResult result=mongoTemplate.getCollection("users").updateOne(
                query.getQueryObject(),
                pipeline
        );
       if(result.getMatchedCount()==0){
           throw new ResourceNotFoundException("user not found");
       }
        System.out.println("the review has been correctly saved in user collection");
    }
    private void insertIntoCarCollection(Document newReview,String carId,String username){
        ObjectId objectId=new ObjectId(carId);
        Query query=new Query(Criteria.where("_id").is(objectId));
        Document newReviewForCar=new Document(newReview);
        newReviewForCar.remove("car_name");
        newReviewForCar.append("username",username);
        System.out.println("username:"+newReviewForCar.get("username"));

        List<Document> pipeline=Arrays.asList(
                new Document("$set",new Document("Top_Ten_Review",
                        new Document("$concatArrays",Arrays.asList(
                                Collections.singletonList(newReviewForCar),
                                new Document("$ifNull",Arrays.asList("$Top_Ten_Review",Arrays.asList()))

                        ))
                )),
                new Document("$set",new Document("Other_review",
                        new Document("$concatArrays",Arrays.asList(
                                new Document("$ifNull",Arrays.asList("$Other_review",Arrays.asList())),
                                new Document("$cond",Arrays.asList(
                                        new Document("$gt",Arrays.asList(new Document("$size","$Top_Ten_Review"),10)),
                                        Arrays.asList(new Document("$arrayElemAt",Arrays.asList("$Top_Ten_Review._id",10))),
                                        Arrays.asList()
                                ))

                        )))),
                new Document("$set",new Document("Top_Ten_Review",
                        new Document("$slice",Arrays.asList("$Top_Ten_Review",10))))
        );

       UpdateResult res=mongoTemplate.getCollection("cars").updateOne(
                query.getQueryObject(),
                pipeline
        );
       if(res.getMatchedCount()==0){
           throw new ResourceNotFoundException("car not found");
       }
        System.out.println("the review has been correctly saved in car collection");
    }
}
