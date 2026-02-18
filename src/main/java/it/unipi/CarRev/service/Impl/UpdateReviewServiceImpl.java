package it.unipi.CarRev.service.Impl;

import it.unipi.CarRev.dao.mongo.ReviewDAO;
import it.unipi.CarRev.dto.ReviewUpdateRequestDTO;
import it.unipi.CarRev.mapper.AutoReviewMapper;
import it.unipi.CarRev.mapper.ReviewMapper;
import it.unipi.CarRev.model.Car;
import it.unipi.CarRev.model.Review;
import it.unipi.CarRev.model.User;
import it.unipi.CarRev.service.exception.BadRequestException;
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
    private record ReviewInfo(Double score,Integer year,Double mileage,String text){}

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
        updateCarReview(DTO);
        updateReviewReview(DTO);

    }
    private void updateCarReview(ReviewUpdateRequestDTO reviewUpdateRequestDTO){
        ObjectId objReviewId=new ObjectId(reviewUpdateRequestDTO.getId());
        Double oldScore=null;
        Integer oldYear=null;
        Double oldMileage=null;
        String oldText=null;

        Query query=new Query(new Criteria().orOperator(
                Criteria.where("Top_Ten_Review._id").is(objReviewId),
                Criteria.where("Other_review._id").is(objReviewId)
        ));
        Car oldCar=mongoTemplate.findOne(query,Car.class);
        if(oldCar==null){
            System.out.println("The car doesent exists anymore");
            return;
        }
        Document review=oldCar.getTopTenReview().stream()
                .filter(embReview->embReview.get("_id").equals(objReviewId))
                .findFirst().orElse(null);
        if(review==null){
            ReviewInfo reviewInfo=fetchInfo(reviewUpdateRequestDTO.getId());
            oldScore=reviewInfo.score;
            oldYear=reviewInfo.year;
            oldMileage=reviewInfo.mileage;
            oldText=reviewInfo.text;

        }
        else{
            oldScore=review.getDouble("rating");
            oldYear=review.getInteger("year");
            oldMileage=review.getDouble("mileage");
            oldText=review.getString("text");
        }
        if(oldMileage==null){
            oldMileage=0.0;
        }
        String newText= reviewUpdateRequestDTO.getText()!=null? reviewUpdateRequestDTO.getText() : oldText;
        Double newScore=reviewUpdateRequestDTO.getRating()!=null? reviewUpdateRequestDTO.getRating():oldScore;
        Double newMileage=reviewUpdateRequestDTO.getMileage()!=null?reviewUpdateRequestDTO.getMileage():oldMileage;
        Integer newYear=reviewUpdateRequestDTO.getYear()!=null?reviewUpdateRequestDTO.getYear():oldYear;
        if(newYear!=null){
            if (newYear < oldCar.getProduction_year()) {
                throw new BadRequestException("you cannot update a review to a year before production_year");
            }
        }
        if (newYear == null && newMileage != 0.0) {
            throw new BadRequestException("Cannot provide a mileage if the year is not specified");
        }
        Integer netChange=0;
        if (oldMileage == 0.0&&newMileage>0.0)netChange=1;
        else if (oldMileage>0.0&&newMileage==0.0) netChange = -1;
        Double deltaScore=newScore-oldScore;
        Double deltaMileage=newMileage-oldMileage;
        Integer decrNum=oldMileage>0.0?-1:0;
        Integer incrNum=newMileage>0.0?1:0;
        //Integer upNum=reviewUpdateRequestDTO.getMileage()>0.0?1:0;
        /*
        Document yearMapping;
        if(oldYear.equals(newYear)){
            yearMapping=new Document("$map",new Document()
                    .append("input","$Product_Year")
                    .append("as","yearDocu")
                    .append("in",new Document("$cond",Arrays.asList(
                            new Document("$eq",Arrays.asList("$$yearDocu.Year",oldYear)),
                            recalculateYear("$$yearDocu",deltaMileage,netChange),
                            "$$yearDocu"
                    ))));
        }
        else{
            yearMapping=new Document("$map",new Document()
                    .append("input","$Product_Year")
                    .append("as","yearDocu")
                    .append("in",new Document("$cond",Arrays.asList(
                            new Document("$eq",Arrays.asList("$$yearDocu.Year",oldYear)),
                            recalculateYear("$$yearDocu",-oldMileage,decrNum),
                            new Document("$cond",Arrays.asList(
                                    new Document("$eq",Arrays.asList("$$yearDocu.Year",newYear)),
                                    recalculateYear("$$yearDocu", newMileage,incrNum),
                                    "$$yearDocu"
                            ))
                    ))));
        }*/
        Document fieldsToUpdate = new Document();
        fieldsToUpdate.append("rating", newScore);
        fieldsToUpdate.append("text", newText);
        if (newYear != null) {
            fieldsToUpdate.append("year", newYear);
        }
        if (newMileage != null && newMileage != 0.0) {
            fieldsToUpdate.append("mileage", newMileage);
        }
        AggregationUpdate update = AggregationUpdate.update()
                .set("Top_Ten_Review").toValue(
                        new Document("$map", new Document()
                                .append("input", "$Top_Ten_Review")
                                .append("as", "rev")
                                .append("in", new Document("$cond", Arrays.asList(
                                        new Document("$eq", Arrays.asList("$$rev._id", objReviewId)),
                                        new Document("$mergeObjects", Arrays.asList("$$rev", fieldsToUpdate)),
                                        "$$rev"
                                ))))
                )
                .set("total_review_score").toValue(new Document("$add", Arrays.asList("$total_review_score", deltaScore)))
                .set("general_rating").toValue(new Document("$divide", Arrays.asList("$total_review_score", "$number_of_reviews")));
               // .set("Product_Year").toValue(yearMapping);

        mongoTemplate.updateFirst(query,update,Car.class);
        /*
        if(!oldYear.equals(reviewUpdateRequestDTO.getYear())){
            Query insertIfNew=new Query(
                    Criteria.where("Top_Ten_Review._id").is(objReviewId)
                            .and("Product_Year.Year").ne(newYear)
            );
            Document newYearDocu = new Document()
                    .append("Year", newYear)
                    .append("Total_Mileage", newMileage)
                    .append("Num_Review_Year",incrNum)
                    .append("Average_Mileage", newMileage);
            mongoTemplate.updateFirst(insertIfNew,new org.springframework.data.mongodb.core.query.Update().push("Product_Year",newYearDocu),Car.class);

        }
         */
    System.out.println("review correctly modified in cars collection");
    }
    private Document recalculateYear(String variable,Double deltaMileage,Integer deltaNum){
        Document newTotal = new Document("$add", Arrays.asList(variable + ".Total_Mileage", deltaMileage));
        Document newCount = new Document("$add", Arrays.asList(variable + ".Num_Review_Year", deltaNum));
        return new Document()
                .append("Year", variable + ".Year")
                .append("Total_Mileage", newTotal)
                .append("Num_Review_Year", newCount)
                .append("Average_Mileage", new Document("$cond", Arrays.asList(
                        new Document("$gt", Arrays.asList(newCount, 0)),
                        new Document("$divide", Arrays.asList(newTotal, newCount)),
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
                Criteria.where("otherReviews._id").is(objReviewId))
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
                        new Document("$map",new Document()
                                .append("input","$reviews")
                                .append("as","rev")
                                .append("in",new Document("$cond", Arrays.asList(
                                        new Document("$eq",Arrays.asList("$$rev._id",objReviewId)),
                                        new Document("$mergeObjects",Arrays.asList("$$rev",newDoc)),
                                        "$$rev"
                                ))))
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
                    actualReview.getMileage(),
                    actualReview.getText()
            );
        }
        else{
            throw new ResourceNotFoundException("the review does not exists");
        }
    }

}
