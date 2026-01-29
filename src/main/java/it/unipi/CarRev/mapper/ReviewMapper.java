package it.unipi.CarRev.mapper;


import it.unipi.CarRev.model.Review;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {
    public Review mapDocumentToReview(Document docReview){
        Review review=new Review();
        review.setId(docReview.getObjectId("_id").toHexString());
        review.setCarName(docReview.getString("Car_Name"));
        review.setText("Text");
        review.setRating(docReview.getDouble("Rating"));
        if (docReview.get("Timestamp") instanceof java.util.Date date){
            review.setTimestamp(date.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime());
        }
        review.setLikes(docReview.getInteger("Likes",0));
        review.setReport(docReview.getInteger("Report",0));
        if(docReview.containsKey("Mileage")){
            review.setMileage(docReview.getInteger("Mileage"));
        }
        if(docReview.containsKey("Year")){
            review.setYear(docReview.getInteger("Year"));
        }
        return review;
    }
}
