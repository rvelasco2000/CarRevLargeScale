package it.unipi.CarRev.service.Impl;

import it.unipi.CarRev.dao.mongo.ReviewDAO;
import it.unipi.CarRev.dto.ReviewUpdateRequestDTO;
import it.unipi.CarRev.mapper.AutoReviewMapper;
import it.unipi.CarRev.mapper.ReviewMapper;
import it.unipi.CarRev.model.Car;
import it.unipi.CarRev.model.Review;
import it.unipi.CarRev.model.User;
import it.unipi.CarRev.service.exception.ForbiddenException;
import it.unipi.CarRev.service.exception.ResourceNotFoundException;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationUpdate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Optional;

@Service
public class UpdateReviewServiceImpl {
    private final MongoTemplate mongoTemplate;
    private final ReviewDAO reviewDAO;
    private final AutoReviewMapper autoReviewMapper;
    private record ReviewInfo(Double score,Integer year,Double mileage){}

    public UpdateReviewServiceImpl(MongoTemplate mongoTemplate, ReviewDAO reviewDAO, AutoReviewMapper autoReviewMapper, ReviewMapper reviewMapper){
        this.mongoTemplate=mongoTemplate;
        this.reviewDAO=reviewDAO;
        this.autoReviewMapper=autoReviewMapper;
    }

    @Transactional("mongoTransactionManager")
    public void updateReview(ReviewUpdateRequestDTO DTO){
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        String username=null;
        if(auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            username = auth.getName();
            System.out.println("username="+username);
        }
        else{
            throw new RuntimeException("User must be authenticated");
        }
        updateUserReview(DTO,username);
        String newText=DTO.getText();
        Double newRating=DTO.getRating();
        Integer newYear=DTO.getYear();
        Double newMileage=DTO.getMileage();

    }
    private void updateCarReview(ReviewUpdateRequestDTO reviewUpdateRequestDTO){
        ObjectId objReviewId=new ObjectId(reviewUpdateRequestDTO.getId());
        Double oldScore=null;
        Integer oldYear=null;
        Double oldMileage=null;

        Query query=new Query(new Criteria().orOperator(
                Criteria.where("Top_Ten_Review._id").is(objReviewId),
                Criteria.where("Other_review").is(objReviewId)
        ));
        Car oldCar=mongoTemplate.findOne(query,Car.class);

        Document review=oldCar.getTopTenReview().stream()
                .filter(embReview->embReview.get("_id").equals(objReviewId))
                .findFirst().orElse(null);
        if(review==null){
            ReviewInfo reviewInfo=fetchInfo(reviewUpdateRequestDTO.getId());
            oldScore=reviewInfo.score;
            oldYear=reviewInfo.year;
            oldMileage=reviewInfo.mileage;
        }
        else{
            oldScore=review.getDouble("rating");
            oldYear=review.getInteger("year");
            oldMileage=review.getDouble("mileage");
        }
        Integer netChange=0;
        if (oldMileage == 0.0 && reviewUpdateRequestDTO.getMileage() > 0.0)netChange=1;
        else if (oldMileage>0.0&&reviewUpdateRequestDTO.getMileage()==0.0) netChange = -1;
        Double deltaScore=reviewUpdateRequestDTO.getRating()-oldScore;
        Double deltaMileage=reviewUpdateRequestDTO.getMileage()-oldMileage;
        Integer decrNum=oldMileage>0.0?-1:0;
        Integer upNum=reviewUpdateRequestDTO.getMileage()>0.0?1:0;
        Document yearDocu=new Document("$map",new Document()
                .append("input","$Product_Year")
                .append("as","docu")
                .append("in",new Document("$cond",Arrays.asList(
                        new Document("$eq",Arrays.asList("$$docu.Year",oldYear)),
                        recalculateYear("$$docu",deltaMileage,decrNum),
                        new Document("$cond",Arrays.asList(
                                new Document("$eq",Arrays.asList("$$docu.Year",reviewUpdateRequestDTO.getYear())),
                                recalculateYear("$$docu",deltaMileage,upNum),
                                "$$docu"
                        ))
                        ))));

    }
    private Document recalculateYear(String variable,Double deltaMileage,Integer decrNum){
        return new Document()
                .append("Year",variable+".Year")
                .append("Total_Mileage",new Document("$add",Arrays.asList(variable+".Total_Mileage",deltaMileage)))
                .append("Num_Review_Year",new Document("$add",Arrays.asList(variable+".Num_Review_Year",decrNum)))
                .append("Average_Mileage",new Document("$cond",Arrays.asList(
                        new Document("$gt",Arrays.asList(new Document("$add",Arrays.asList(variable+".Num_Review_Year",decrNum)),0)),
                        new Document("$divide",Arrays.asList(
                                new Document("$add",Arrays.asList(variable+".Total_Mileage", deltaMileage)),
                                new Document("$add",Arrays.asList(variable+".Num_Review_Year",decrNum)))),
                                0.0
                        )));
    }
    private void updateReviewReview(ReviewUpdateRequestDTO reviewUpdateRequestDTO){
        Review review= reviewDAO.findById(reviewUpdateRequestDTO.getId())
                .orElseThrow(()->new ResourceNotFoundException("the review has not been found"));
        autoReviewMapper.updateReviewFromDto(reviewUpdateRequestDTO,review);
        reviewDAO.save(review);
        System.out.println("Review correctly updated in review collection");
    }
    private void updateUserReview(ReviewUpdateRequestDTO reviewUpdateRequestDTO,String username){
        ObjectId objReviewId=new ObjectId(reviewUpdateRequestDTO.getId());

        Query query=new Query(Criteria.where("username").is(username).orOperator(
                Criteria.where("reviews._id").is(objReviewId),
                Criteria.where("otherReviews").is(objReviewId))
        );
        Document newDoc=new Document();
        newDoc.append("_id",objReviewId);
        if(reviewUpdateRequestDTO.getText()!=null)
        {
            newDoc.append("text",reviewUpdateRequestDTO.getText());
        }
        if(reviewUpdateRequestDTO.getYear()!=null){
            newDoc.append("year",reviewUpdateRequestDTO.getYear());
        }
        if(reviewUpdateRequestDTO.getMileage()!=null){
            newDoc.append("mileage",reviewUpdateRequestDTO.getMileage());
        }
        AggregationUpdate update=AggregationUpdate.update()
                .set("reviews").toValue(
                        new Document()
                                .append("input","$reviews")
                                .append("as","rev")
                                .append("in",new Document("$cond", Arrays.asList(
                                        new Document("$eq",Arrays.asList("$$rev._id",objReviewId)),
                                        new Document("$mergeObjects",Arrays.asList("$$rev",newDoc)),
                                        "$$rev"
                                )))
                );
        User user=mongoTemplate.findAndModify(query,update,User.class);
        if(user==null){
            throw new ForbiddenException("User does not own this review");
        }
        System.out.println("review updated correctly in user");

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

}
