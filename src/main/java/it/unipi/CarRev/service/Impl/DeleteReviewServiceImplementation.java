package it.unipi.CarRev.service.Impl;

import com.mongodb.client.result.UpdateResult;
import it.unipi.CarRev.dao.mongo.ReviewDAO;
import it.unipi.CarRev.dao.mongo.UserDAO;
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
        reviewDAO.deleteById(reviewId);
        System.out.println("review correctly removed from Review collection");
        return true;
    }
    private void deleteInUser(String reviewId){
        ObjectId objReviewId=new ObjectId(reviewId);
        Query query=new Query(Criteria.where("reviews._id").is(objReviewId));
        org.springframework.data.mongodb.core.query.Update update =new Update().pull("reviews",new Document("_id",objReviewId));
        UpdateResult result=mongoTemplate.updateFirst(query,update, User.class);
        if(result.getMatchedCount()==0){
            //insert here otherReviews delete
        }

    }
}
