package it.unipi.CarRev.service.Impl;

import it.unipi.CarRev.dao.mongo.CarDAO;
import it.unipi.CarRev.dao.mongo.ReviewDAO;
import it.unipi.CarRev.dao.mongo.projection.CarRevAndOtherReviews;
import it.unipi.CarRev.dto.ReviewResponseDTO;
import it.unipi.CarRev.model.Review;
import it.unipi.CarRev.service.exception.ResourceNotFoundException;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ShowMostLikedReviewsServiceImplementation {

    private final CarDAO carDAO;
    private final ReviewDAO reviewDAO;
    public  ShowMostLikedReviewsServiceImplementation(CarDAO carDAO, ReviewDAO reviewDAO){
        this.carDAO=carDAO;
        this.reviewDAO=reviewDAO;
    }
    public Page<ReviewResponseDTO> getMostLikedReviews(String carId, Pageable pageable){
        ObjectId objCarId=new ObjectId(carId);
        CarRevAndOtherReviews reviews=carDAO.findReviewsById(carId);
        List<Document> topTenReviews= reviews.getTopTenReview()!=null ? reviews.getTopTenReview() : Collections.emptyList();
        List<Document> otherReviews= reviews.getOtherReview()!=null ? reviews.getOtherReview() : Collections.emptyList();
        List<Document> sortedReviews=getSortedReviews(topTenReviews,otherReviews);
        int start=(int) pageable.getOffset();
        if(start>=sortedReviews.size()){
            return new PageImpl<>(Collections.emptyList(),pageable,sortedReviews.size());
        }
        int end=(int)Math.min(start+pageable.getPageSize(),sortedReviews.size());
        List<Document> documentInPage=sortedReviews.subList(start,end);
        List<String> idsToFetch=documentInPage.stream()
                .filter(doc->doc.getString("text")==null)
                .map(doc->doc.getObjectId("_id").toHexString())
                .toList();
        Map<String, Review> externalReviews = new HashMap<>();
        if (!idsToFetch.isEmpty()) {
            reviewDAO.findAllById(idsToFetch).forEach(r -> externalReviews.put(r.getId(), r));
        }
        List<ReviewResponseDTO> content = documentInPage.stream()
                .map(doc -> {
                    String id = doc.getObjectId("_id").toHexString();
                    Integer likes = doc.getInteger("likes", 0);
                    if (doc.getString("text") != null) {
                        return mapReviewToDTO(doc);
                    } else {
                        return mapExternalReviewToDTO(doc,externalReviews.get(id));
                    }
                })
                .toList();
        return new PageImpl<>(content,pageable,sortedReviews.size());



    }
    private List<Document> getSortedReviews(List<Document> topTenReviews, List<Document> otherReviews) {
        List<Document> allReviews = Stream.concat(
                topTenReviews.stream(),
                otherReviews.stream()
        ).sorted(Comparator.comparingInt(doc -> {
            Document newDoc = (Document) doc;
            return newDoc.getInteger("likes", 0);
        }).reversed()).collect(Collectors.toList());

        return allReviews;
    }
    private ReviewResponseDTO mapReviewToDTO(Document review){
        ReviewResponseDTO newDto=new ReviewResponseDTO();
        newDto.setId(review.getObjectId("_id").toHexString());
        newDto.setText(review.getString("text"));
        newDto.setLikes(review.getInteger("likes"));
        newDto.setRating(review.getDouble("rating"));
        newDto.setUsername(review.getString("username"));
        if(review.getInteger("year")!=null){
            newDto.setYear(review.getInteger("year"));
        }
        
        if(review.getDouble("mileage")!=null){
            newDto.setMileage(review.getDouble("mileage"));
        }
        return newDto;
    }
    private ReviewResponseDTO mapExternalReviewToDTO(Document embeddedInfo,Review externalInfo){
        ReviewResponseDTO newDto=new ReviewResponseDTO();
        newDto.setId(embeddedInfo.getObjectId("_id").toHexString());
        newDto.setLikes(embeddedInfo.getInteger("likes"));
        newDto.setText(externalInfo.getText());
        newDto.setRating(externalInfo.getRating());
        newDto.setUsername(externalInfo.getUsername());
        if(externalInfo.getYear()!=null){
            newDto.setYear(externalInfo.getYear());
        }
        if(externalInfo.getMileage()!=null){
            newDto.setMileage(externalInfo.getMileage());
        }
        return newDto;


    }
}
