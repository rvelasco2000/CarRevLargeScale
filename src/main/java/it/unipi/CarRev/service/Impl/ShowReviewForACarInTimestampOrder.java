package it.unipi.CarRev.service.Impl;


import it.unipi.CarRev.dao.mongo.ReviewDAO;
import it.unipi.CarRev.dto.ReviewResponseDTO;
import it.unipi.CarRev.model.Review;
import it.unipi.CarRev.service.exception.ResourceNotFoundException;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/***
 * this service will be called when the user click on more reviews;
 */
@Service
public class ShowReviewForACarInTimestampOrder {
    private final MongoTemplate mongoTemplate;
    private final ReviewDAO reviewDAO;
    private static final Integer PAGESIZE=10;
    public ShowReviewForACarInTimestampOrder(MongoTemplate mongoTemplate, ReviewDAO reviewDAO){
        this.mongoTemplate=mongoTemplate;
        this.reviewDAO=reviewDAO;
    }
    public Page<ReviewResponseDTO> getReviewsByTimestamp(String carId,Integer numPage){
        ObjectId objectCarId=new ObjectId(carId);
        Integer start=numPage*PAGESIZE;
        Aggregation aggregation=Aggregation.newAggregation(
                Aggregation.match(Criteria.where("_id").is(objectCarId)),
                Aggregation.project()
                        .and(ArrayOperators.ReverseArray.reverseArrayOf("Other_review")).as("timestamp_ordered_reviews")
                        .and(ArrayOperators.Size.lengthOfArray("Other_review")).as("total_count"),
                Aggregation.project("total_count")
                        .and(ArrayOperators.Slice.sliceArrayOf("timestamp_ordered_reviews").offset(start).itemCount(PAGESIZE))
                        .as("paginated_reviews")
        );
        AggregationResults<Document> aggregationResults=mongoTemplate.aggregate(aggregation,"cars", Document.class);
        Document resultDocument=aggregationResults.getUniqueMappedResult();
        if(resultDocument==null){
            throw new ResourceNotFoundException("the car does not exists");
        }
        if(!resultDocument.containsKey("paginated_reviews")){
            return new PageImpl<>(new ArrayList<>(), PageRequest.of(numPage,PAGESIZE),0);
        }
        List<Document> otherReviewDocument=resultDocument.getList("paginated_reviews",Document.class);
        long total=(long)resultDocument.getInteger("total_count",0);
        List<String> listRevId=otherReviewDocument.stream()
                .map(doc->doc.getObjectId("_id").toHexString()).toList();
        List<Review> reviewList=reviewDAO.findAllById(listRevId);
        Map<String, Review> reviewMap = reviewList.stream()
                .collect(Collectors.toMap(Review::getId, r -> r));
        List<ReviewResponseDTO> returnDto=otherReviewDocument.stream()
                .map(reviewDoc->{
                    String id=reviewDoc.getObjectId("_id").toHexString();
                    Review review=reviewMap.get(id);
                    ReviewResponseDTO reviewResponseDTO=new ReviewResponseDTO();
                    if(review!=null){
                        reviewResponseDTO.setId(id);
                        reviewResponseDTO.setUsername(review.getUsername());
                        reviewResponseDTO.setText(review.getText());
                        reviewResponseDTO.setRating(review.getRating());
                        reviewResponseDTO.setLikes(reviewDoc.getInteger("likes"));
                        if(review.getYear()!=null){
                            reviewResponseDTO.setYear(review.getYear());
                        }
                        if(review.getMileage()!=null){
                            reviewResponseDTO.setMileage(review.getMileage());
                        }
                    }
                    return reviewResponseDTO;
                }).toList();
        return new PageImpl<>(returnDto, PageRequest.of(numPage, PAGESIZE), total);
    }
}
