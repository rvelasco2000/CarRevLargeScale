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
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

/***
 * since the delete of a review is something that we do sporadically we have decided to create something more complex
 * and decided to put the promotion of a review here
 */
@Service
public class DeleteReviewServiceImplementation{
    private final ReviewDAO reviewDAO;
    private record ReviewInfo(Double score,Integer year,Double mileage){}
    private final MongoTemplate mongoTemplate;
    public DeleteReviewServiceImplementation(ReviewDAO reviewDAO,MongoTemplate mongoTemplate){
        this.reviewDAO=reviewDAO;
        this.mongoTemplate=mongoTemplate;
    }

    /***
     * this method allow us to delete a review without breaking the integrity between the reviews,users,cars collection
     * it also correctly update the average score of a car and the average mileage for each year
     * @param reviewId: the Id of the review i want to delete
     */
    @Transactional("mongoTransactionManager")
    public void deleteAReview(String reviewId){
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        Boolean isAdmin=false;
        String username=null;
        if(auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)){
            username = auth.getName();
            System.out.println("username="+username);
            isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }
        else{
            throw new RuntimeException("User must be authenticated");
        }
       // Double rating=fetchScore(reviewId);
        Boolean result=deleteInUser(reviewId,username,isAdmin);
        if(!result){
            System.out.println("user:"+username+" does not own the review");
            throw new ResourceNotFoundException("the user does not own this review");
        }
        System.out.println("review correctly removed from users collection");
        deleteInCar(reviewId);
        System.out.println("review correctly removed from car collection");
        reviewDAO.deleteById(reviewId);
        System.out.println("review correctly removed from reviews collection");
        //deleteScoreReviewAfterCommit(carId,rating);
        //System.out.println("correctly migrated deleted review info form mongoDB to redis");
    }
    private ReviewInfo fetchInfo(String reviewId){
        Optional<Review> review=reviewDAO.findById(reviewId);
        if(review.isPresent()){
           Review actualReview=review.get();
           return new ReviewInfo(
                   actualReview.getRating(),
                   actualReview.getYear(),
                   actualReview.getMileage()
           );
        }
        else{
            throw new ResourceNotFoundException("the review does not exists");
        }
    }
    /*
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
    }*/
    private void deleteInCar(String reviewId){
        ObjectId objReviewId=new ObjectId(reviewId);
        Double score=null;
        Integer year=null;
        Double mileage=null;

        Query query=new Query(new Criteria().orOperator(
                Criteria.where("Top_Ten_Review._id").is(objReviewId),
                Criteria.where("Other_review._id").is(objReviewId)
        ));
        Car oldCar=mongoTemplate.findOne(query,Car.class);
        if(oldCar==null){
            System.out.println("the car of this review has been deleted");
            return;
        }
        /*
        Double score=oldCar.getTopTenReview().stream()
                .filter(embReview->embReview.get("_id").equals(objReviewId))
                .findFirst()
                .map(rev->rev.getDouble("rating")).orElse(null);

         */
        //here i check if the car is present in the latest_review to avoid lookin int reviews collection
        Document review=oldCar.getTopTenReview().stream()
                .filter(embReview->embReview.get("_id").equals(objReviewId))
                .findFirst().orElse(null);
        if(review==null){
            ReviewInfo reviewInfo=fetchInfo(reviewId);
            score=reviewInfo.score;
            year=reviewInfo.year;
            mileage=reviewInfo.mileage;

        }
        else{
            score=review.getDouble("rating");
            year=review.getInteger("year");
            mileage=review.getDouble("mileage");

        }
        Integer decRew= mileage==null || mileage==0.0 ? 0:1;
        System.out.println("score of the eliminated review:"+score);
        AggregationUpdate update=AggregationUpdate.update()
                .set("Top_Ten_Review").toValue(ArrayOperators.Filter.filter("Top_Ten_Review")
                        .as("review")
                        .by(ComparisonOperators.Ne.valueOf("review._id").notEqualToValue(objReviewId)))
                .set("Other_review").toValue(ArrayOperators.Filter.filter("Other_review")
                        .as("linkedRev")
                        .by(ComparisonOperators.Ne.valueOf("linkedRev._id").notEqualToValue(objReviewId)))
                .set("total_review_score").toValue(ArithmeticOperators.Subtract.valueOf("total_review_score").subtract(score))
                .set("number_of_reviews").toValue(ArithmeticOperators.Subtract.valueOf("number_of_reviews").subtract(1))
                .set("general_rating").toValue(
                        ConditionalOperators.when(Criteria.where("number_of_reviews").gt(0))
                                .then(ArithmeticOperators.Divide.valueOf("total_review_score").divideBy("number_of_reviews"))
                                .otherwise(0)
                );
        /*
        if(year!=null){
            update=update.set("Product_Year").toValue(
                    new Document("$map",new Document()
                            .append("input","$Product_Year")
                            .append("as","yearDocu")
                            .append("in",new Document("$cond",Arrays.asList(
                                    new Document("$eq",Arrays.asList("$$yearDocu.Year",year)),
                                    new Document()
                                            .append("Year","$$yearDocu.Year")
                                            .append("Total_Mileage",new Document("$subtract",Arrays.asList("$$yearDocu.Total_Mileage",mileage)))
                                            .append("Num_Review_Year",new Document("$subtract",Arrays.asList("$$yearDocu.Num_Review_Year",decRew)))
                                            .append("Average_Mileage",new Document("$cond",Arrays.asList(
                                                            new Document("$gt",Arrays.asList(new Document("$subtract",Arrays.asList("$$yearDocu.Num_Review_Year",decRew)),0)),
                                                            new Document("$divide",Arrays.asList(
                                                                    new Document("$subtract",Arrays.asList("$$yearDocu.Total_Mileage",mileage)),
                                                                    new Document("$subtract",Arrays.asList("$$yearDocu.Num_Review_Year",decRew))
                                                    )),
                                                    0.0
                            ))),"$$yearDocu")))
                    )
            );
        }

         */
                /*
        Car oldCar=mongoTemplate.findOne(query,Car.class);
        Double score=oldCar.getTopTenReview().stream()
                .filter(embReview->embReview.get("_id").equals(objReviewId))
                .findFirst()
                .map(rev->rev.getDouble("rating")).orElse(null);
        if(score==null){
            score=fetchScore(reviewId);
        }
        System.out.println("score of the eliminated review:"+score);
        Update update=new Update()
                .pull("Top_Ten_Review",new Document("_id",objReviewId))
                .pull("Other_review",objReviewId)
                .inc("total_review_score",-score)
                .inc("number_of_reivews",-1);

         */
        Car car=mongoTemplate.findAndModify(query,update,FindAndModifyOptions.options().returnNew(true),Car.class);
        if(car==null){
            throw new ResourceNotFoundException("car not found");
        }
        if(car.getTopTenReview().size()<10 && !car.getOtherReview().isEmpty()){
            promoteAReview(car.getId(),car.getOtherReview().getLast().getObjectId("_id"),car.getOtherReview().getLast().getInteger("likes"));
        }
        //updateReview(car.getId());
       // return car.getId();
    }
    /*
    private void updateReview(String carId){
        Query query=new Query(Criteria.where("_id").is(carId));
        AggregationUpdate update=AggregationUpdate.update()
                .set("general_rating").toValue(
                        ConditionalOperators.when(Criteria.where("number_of_reviews").gt(0))
                                .then(ArithmeticOperators.Divide.valueOf("total_review_score").divideBy("number_of_reviews"))
                                .otherwise(0)
                );
        mongoTemplate.updateFirst(
                query,
                update,
                Car.class
        );
    }
     */
    private Boolean deleteInUser(String reviewId,String username,Boolean isAdmin){
        ObjectId objReviewId=new ObjectId(reviewId);
        Query query=new Query();
        if(isAdmin){
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("reviews._id").is(objReviewId),
                    Criteria.where("otherReviews._id").is(objReviewId)
            ));
        }
        else {
            query.addCriteria(Criteria.where("username").is(username).orOperator(
                    Criteria.where("reviews._id").is(objReviewId),
                    Criteria.where("otherReviews._id").is(objReviewId))
            );
        }
        Update update=new Update()
                .pull("reviews",new Document("_id",objReviewId))
                .pull("otherReviews",new Document("_id",objReviewId));
       // UpdateResult updateResult=mongoTemplate.updateFirst(query,update,User.class);
        User user=mongoTemplate.findAndModify(query,update,FindAndModifyOptions.options().returnNew(true),User.class);

        if(user==null){
            return isAdmin;
        }
        if(isAdmin){
            username=user.getUsername();
        }
        if(user.getReviews().size()<10 && !user.getOtherReviews().isEmpty()){
            promoteReviewInUser(username,user.getOtherReviews().getLast().getObjectId("_id"),user.getOtherReviews().getLast().getInteger("likes"));
        }
       return true;
    }
    //very unlikely that a user will have more than 10 reviews
    private void promoteReviewInUser(String username,ObjectId promotedReviewId,Integer likes){
        Optional<Review> optReview=reviewDAO.findById(String.valueOf(promotedReviewId));
        if(optReview.isEmpty()){
            throw new ResourceNotFoundException("review to promote not found");
        }
        Review review=optReview.get();
        Document newReview=new Document()
                .append("_id",promotedReviewId)
                .append("car_name",review.getCarName())
                .append("text",review.getText())
                .append("rating",review.getRating())
                .append("timestamp",review.getTimestamp())
                .append("likes",likes)
                .append("report",review.getReport());
        if(review.getYear()!=null){
            newReview.append("year",review.getYear());
        }
        if(review.getMileage()!=null){
            newReview.append("mileage",review.getMileage());
        }
        Query query=new Query(Criteria.where("username").is(username));
        Update update = new Update()
                .push("reviews",newReview)
                .pull("otherReviews",new Document("_id",promotedReviewId));
        mongoTemplate.updateFirst(query, update, User.class);
    }
    private void promoteAReview(String carId,ObjectId nextReviewId,Integer likes){
        Optional<Review> optReview=reviewDAO.findById(String.valueOf(nextReviewId));
        if(optReview.isEmpty()){
            throw new ResourceNotFoundException("review to promote not found");
        }
        Review review=optReview.get();
        Document toInputReview=new Document()
                .append("_id",nextReviewId)
                .append("username",review.getUsername())
                .append("text",review.getText())
                .append("rating",review.getRating())
                .append("timestamp",review.getTimestamp())
                .append("likes",likes)
                .append("report",review.getReport());
        if(review.getYear()!=null){
            toInputReview.append("year",review.getYear());
        }
        if(review.getMileage()!=null){
            toInputReview.append("mileage",review.getMileage());
        }
        Query query=new Query(Criteria.where("_id").is(carId));
        Update update = new Update()
                .push("Top_Ten_Review",toInputReview)
                .pull("Other_review",new Document("_id",nextReviewId));
        mongoTemplate.updateFirst(query, update, Car.class);
    }
}
