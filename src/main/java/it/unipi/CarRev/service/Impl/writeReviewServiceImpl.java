package it.unipi.CarRev.service.Impl;

import it.unipi.CarRev.dao.mongo.CarDAO;
import it.unipi.CarRev.dao.mongo.ReviewDAO;
import it.unipi.CarRev.dao.mongo.UserDAO;
import it.unipi.CarRev.dao.mongo.projection.CarName;
import it.unipi.CarRev.dto.InsertReviewRequestDTO;
import it.unipi.CarRev.mapper.ReviewMapper;
import it.unipi.CarRev.model.Review;
import it.unipi.CarRev.model.User;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Service
public class writeReviewServiceImpl {
    private final UserDAO userDAO;
    private final CarDAO carDAO;
    private final MongoTemplate mongoTemplate;
    private final ReviewDAO reviewDAO;
    public writeReviewServiceImpl(UserDAO userDAO,CarDAO carDAO,MongoTemplate mongoTemplate,ReviewDAO reviewDAO){
        this.userDAO=userDAO;
        this.carDAO=carDAO;
        this.mongoTemplate=mongoTemplate;
        this.reviewDAO=reviewDAO;
    }

    public boolean writeReview(InsertReviewRequestDTO insertReviewRequestDTO){
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        String username=null;
        //this check should be handled by the security config but for good measure i will keep it
        if(auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            username = auth.getName();
        }
        else{
            return false;
        }
        CarName carName=carDAO.findCarById(insertReviewRequestDTO.getCarId());
        if(carName==null){
            return false;
        }
        Document newReview=new Document()
                .append("_id",new ObjectId())
                .append("Car_name",carName.getcarName())
                .append("Text",insertReviewRequestDTO.getText())
                .append("Rating",insertReviewRequestDTO.getRating())
                .append("Timestamp", LocalDateTime.now())
                .append("Likes",0)
                .append("Report",0);
        ReviewMapper reviewMapper;
        //Review insertReview=reviewMapper.mapReviewFromDto(newReview,);
        //reviewDAO.save();
        if(insertReviewRequestDTO.getYear()!=null){
            newReview.append("Year",insertReviewRequestDTO.getYear());
        }
        if(insertReviewRequestDTO.getMileage()!=null){
            newReview.append("Mileage",insertReviewRequestDTO.getMileage());
        }
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


        return true;
    }
}
