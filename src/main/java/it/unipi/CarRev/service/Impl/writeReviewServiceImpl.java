package it.unipi.CarRev.service.Impl;

import it.unipi.CarRev.dao.mongo.CarDAO;
import it.unipi.CarRev.dao.mongo.ReviewDAO;
import it.unipi.CarRev.dao.mongo.UserDAO;
import it.unipi.CarRev.dao.mongo.projection.CarName;
import it.unipi.CarRev.dto.InsertReviewRequestDTO;
import it.unipi.CarRev.mapper.ReviewMapper;
import it.unipi.CarRev.model.Review;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;


@Service
public class writeReviewServiceImpl {
    private final UserDAO userDAO;
    private final CarDAO carDAO;
    private final MongoTemplate mongoTemplate;
    private final ReviewDAO reviewDAO;
    private final ReviewMapper reviewMapper;
    public writeReviewServiceImpl(UserDAO userDAO,CarDAO carDAO,MongoTemplate mongoTemplate,ReviewDAO reviewDAO,ReviewMapper reviewMapper){
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
            return false;
        }
        Document newReview=returnDocumentFromDTO(insertReviewRequestDTO,carName);
        Review review=reviewMapper.mapDocumentToReview(newReview);
        insertIntoUserCollection(newReview,username);
        reviewDAO.save(review);
        System.out.println("the review has been correctly saved in reviews collection");
        return true;
    }
    private Document returnDocumentFromDTO(InsertReviewRequestDTO insertReviewRequestDTO,CarName carName){
        Document newReview=new Document()
                .append("_id",new ObjectId())
                .append("Car_name",carName.getcarName())
                .append("Text",insertReviewRequestDTO.getText())
                .append("Rating",insertReviewRequestDTO.getRating())
                .append("Timestamp", LocalDateTime.now())
                .append("Likes",0)
                .append("Report",0);
        if(insertReviewRequestDTO.getYear()!=null){
            newReview.append("Year",insertReviewRequestDTO.getYear());
        }
        if(insertReviewRequestDTO.getMileage()!=null){
            newReview.append("Mileage",insertReviewRequestDTO.getMileage());
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

        mongoTemplate.getCollection("users").updateOne(
                query.getQueryObject(),
                pipeline
        );
        System.out.println("the review has been correctly saved in user collection");
    }
}
