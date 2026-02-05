package it.unipi.CarRev.service.Impl;

import com.mongodb.client.result.UpdateResult;
import it.unipi.CarRev.config.RedisConfig;
import it.unipi.CarRev.dao.mongo.ReviewDAO;
import it.unipi.CarRev.dao.mongo.UserDAO;
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
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import redis.clients.jedis.Jedis;

import java.util.Optional;

@Service
public class DeleteReviewServiceImplementation{
    private final ReviewDAO reviewDAO;
    private final MongoTemplate mongoTemplate;
    public DeleteReviewServiceImplementation(ReviewDAO reviewDAO,MongoTemplate mongoTemplate){
        this.reviewDAO=reviewDAO;
        this.mongoTemplate=mongoTemplate;
    }
    @Transactional("mongoTransactionManager")
    public void deleteAReview(String reviewId){
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        String username=null;
        if(auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            username = auth.getName();
            System.out.println("username="+username);
        }
        else{
            throw new RuntimeException("User must be authenticated");
        }
        Double rating=fetchScore(reviewId);
        Boolean result=deleteInUser(reviewId,username);
        if(!result){
            System.out.println("user:"+username+" does not own the review");
            throw new ResourceNotFoundException("the user does not own this review");
        }
        System.out.println("review correctly removed from users collection");
        String carId=deleteInCar(reviewId);
        System.out.println("review correctly removed from car collection");
        reviewDAO.deleteById(reviewId);
        System.out.println("review correctly removed from reviews collection");
        deleteScoreReviewAfterCommit(carId,rating);
        System.out.println("correctly migrated deleted review info form mongoDB to redis");
    }
    private Double fetchScore(String reviewId){
        Optional<Review> review=reviewDAO.findById(reviewId);

        if(review.isPresent()){
           Review actualReview=review.get();
           return actualReview.getRating();
        }
        else{
            throw new ResourceNotFoundException("the review does not exists");
        }
    }
    private void deleteScoreReviewAfterCommit(String carId,Double score){
        if(TransactionSynchronizationManager.isActualTransactionActive()){
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit(){
                    final String SCOREKEY="Cars:"+carId+":totalRating";
                    final String NUMREVKEY="Cars:"+carId+":numberOfReviews";
                    try(Jedis jedis= RedisConfig.getJedis()){
                        jedis.decr(NUMREVKEY);
                        jedis.incrByFloat(SCOREKEY,-score);
                    }
                    catch(Exception e){
                        System.err.print("error during the redis delete of a score");
                    }
                }
            });
        }
    }
    private String deleteInCar(String reviewId){
        ObjectId objReviewId=new ObjectId(reviewId);
        Query query=new Query(new Criteria().orOperator(
                Criteria.where("Top_Ten_Review._id").is(objReviewId),
                Criteria.where("Other_review").is(objReviewId)
        ));
        Update update=new Update()
                .pull("Top_Ten_Review",new Document("_id",objReviewId))
                .pull("Other_review",objReviewId);
        Car car=mongoTemplate.findAndModify(query,update, Car.class);
        if(car==null){
            throw new ResourceNotFoundException("car not found");
        }
        return car.getId();
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
