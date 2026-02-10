package it.unipi.CarRev.mapper;


import it.unipi.CarRev.model.Review;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {
    public Review mapDocumentToReview(Document docReview,String username){
        Review review=new Review();
        review.setId(docReview.getObjectId("_id").toHexString());
        review.setUsername(username);
        review.setCarName(docReview.getString("car_name"));
        review.setText(docReview.getString("text"));
        review.setRating(docReview.getDouble("rating"));
        if (docReview.get("timestamp") instanceof java.time.LocalDateTime ldt){
           review.setTimestamp(ldt);
        }
      // review.setLikes(docReview.getInteger("likes",0));
        review.setReport(docReview.getInteger("report",0));
        if(docReview.containsKey("mileage")){
            review.setMileage(docReview.getDouble("mileage"));
        }
        if(docReview.containsKey("year")){
            review.setYear(docReview.getInteger("year"));
        }
        return review;
    }
}
