package it.unipi.CarRev.service.Impl;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import it.unipi.CarRev.dao.mongo.ReviewDAO;
import it.unipi.CarRev.dao.mongo.UserDAO;
import it.unipi.CarRev.dao.neo4j.DeleteUserNeo4jDAO;
import it.unipi.CarRev.service.Impl.DeleteUserneo4jImpl;
import it.unipi.CarRev.model.Car;
import it.unipi.CarRev.model.Review;
import it.unipi.CarRev.model.User;
import it.unipi.CarRev.service.exception.ForbiddenException;
import it.unipi.CarRev.service.exception.ResourceNotFoundException;
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

import java.util.ArrayList;
import java.util.List;

@Service
public class DeleteUserServiceImplementation {
    private final UserDAO userDAO;
    private final MongoTemplate mongoTemplate;
    private final DeleteUserNeo4jDAO deleteuserneo4jdao;
    public DeleteUserServiceImplementation(UserDAO userDAO, MongoTemplate mongoTemplate ,DeleteUserNeo4jDAO deleteuserneo4jdao){
        this.userDAO=userDAO;
        this.mongoTemplate=mongoTemplate;
        this.deleteuserneo4jdao=deleteuserneo4jdao;
    }
    @Transactional("mongoTransactionManager")
    public void deleteAUser(){
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        String username=null;
        if(auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            username = auth.getName();
            System.out.println("username="+username);
        }
        else{
            throw new ForbiddenException("User must be authenticated");
        }
        User user=userDAO.findByUsername(username).orElse(null);
        long deletedneo4 = deleteuserneo4jdao.deleteUser(username);
        if(user==null){
            throw new ResourceNotFoundException("the user does not exists");
        }
        List<ObjectId> userReviewsId=getAllId(user);
        if(userReviewsId.isEmpty()){
            userDAO.deleteByUsername(username);
            System.out.println("user correctly deleted");
            return;
        }
        String deletedUserUsername="Deleted User";
        Query reviewQuery=new Query(Criteria.where("_id").in(userReviewsId));
        Update reviewUpdate=new Update().set("username",deletedUserUsername);
        mongoTemplate.updateMulti(reviewQuery,reviewUpdate, Review.class);

        Query carQuery=new Query(Criteria.where("Top_Ten_Review._id").in(userReviewsId));
        UpdateOptions carUpdateOptions= new UpdateOptions().arrayFilters(List.of(
                Filters.in("idRev._id",userReviewsId)
        ));
        Update carUpdate=new Update().set("Top_Ten_Review.$[idRev].username",deletedUserUsername);
        mongoTemplate.getCollection("cars").updateMany(
          carQuery.getQueryObject(),
          carUpdate.getUpdateObject(),
                carUpdateOptions
        );
        userDAO.deleteByUsername(username);
        System.out.println("user correctly deleted");
    }
    private List<ObjectId> getAllId(User user){
        List<ObjectId> idList=new ArrayList<>();
        if(user.getReviews()!=null){
            for(Document review: user.getReviews()){
                idList.add(review.getObjectId("_id"));
            }
        }
        if(user.getOtherReviews()!=null){
            for(Document review: user.getOtherReviews()){
                idList.add(review.getObjectId("_id"));
            }
        }
        return idList;
    }

}
