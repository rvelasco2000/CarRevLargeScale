package it.unipi.CarRev.service.Impl;


import com.mongodb.client.result.UpdateResult;
import it.unipi.CarRev.config.MongoConfig;
import it.unipi.CarRev.model.Review;
import it.unipi.CarRev.service.exception.ForbiddenException;
import it.unipi.CarRev.service.exception.ResourceNotFoundException;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
@Service
public class ClearAReviewReportServiceImplementation {
    private final MongoTemplate mongoTemplate;
    public ClearAReviewReportServiceImplementation(MongoTemplate mongoTemplate){
        this.mongoTemplate=mongoTemplate;
    }
    public void clearAReview(String reviewId){
        ObjectId objReviewId=new ObjectId(reviewId);
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated() || (auth instanceof AnonymousAuthenticationToken)) {
           throw new ForbiddenException("only authenticated user can call this endpoint");
        }

        if(!auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))){
            throw new ForbiddenException("only the admin can access this enpoint");
        }

        Query query=new Query(Criteria.where("_id").is(objReviewId));
        Update update=new Update().set("report",0);
        UpdateResult result=mongoTemplate.updateFirst(query,update, Review.class);
        if(result.getMatchedCount()==0){
            throw new ResourceNotFoundException("the selected review does not exists");
        }
        System.out.println("report correctly removed from review");
    }
}
