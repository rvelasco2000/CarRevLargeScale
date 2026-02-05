package it.unipi.CarRev.service.Impl;

import com.mongodb.client.result.UpdateResult;
import it.unipi.CarRev.dao.mongo.ReviewDAO;
import it.unipi.CarRev.dao.mongo.UserDAO;
import it.unipi.CarRev.model.Car;
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

@Service
public class DeleteReviewServiceImplementation{
    private final ReviewDAO reviewDAO;
    private final MongoTemplate mongoTemplate;
    public DeleteReviewServiceImplementation(ReviewDAO reviewDAO,MongoTemplate mongoTemplate){
        this.reviewDAO=reviewDAO;
        this.mongoTemplate=mongoTemplate;
    }
    @Transactional("mongoTransactionManager")
    public boolean deleteAReview(String reviewId){
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        String username=null;
        if(auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            username = auth.getName();
            System.out.println("username="+username);
        }
        else{
            return false;
        }
        Boolean result=deleteInUser(reviewId,username);
        if(!result){
            System.out.println("user:"+username+" does not own the review");
            return false;
        }
        System.out.println("review correctly removed from users collection");
        deleteInCar(reviewId);
        System.out.println("review correctly removed from car collection");
        reviewDAO.deleteById(reviewId);
        System.out.println("review correctly removed from reviews collection");

        return true;
    }
    private void deleteInCar(String reviewId){
        ObjectId objReviewId=new ObjectId(reviewId);
        Query query=new Query(new Criteria().orOperator(
                Criteria.where("Top_Ten_Review._id").is(objReviewId),
                Criteria.where("Other_review").is(objReviewId)
        ));
        Update update=new Update()
                .pull("Top_Ten_Review",new Document("_id",objReviewId))
                .pull("Other_review",objReviewId);
        mongoTemplate.updateFirst(query,update, Car.class);
    }
    private Boolean deleteInUser(String reviewId,String username){
        ObjectId objReviewId=new ObjectId(reviewId);
        Query query=new Query(Criteria.where("username").is(username).orOperator(
                Criteria.where("reviews._id").is(objReviewId),
                Criteria.where("otherReviews").is(objReviewId))
        );
        Update update=new Update()
                .pull("reviews",new Document("_id",objReviewId))
                .pull("otherReviews",objReviewId);
        UpdateResult updateResult=mongoTemplate.updateFirst(query,update,User.class);
        if(updateResult.getMatchedCount()==0){
            return false;
        }
       return true;


    }
}
