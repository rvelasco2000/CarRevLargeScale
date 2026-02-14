package it.unipi.CarRev.service.Impl;


import it.unipi.CarRev.dao.mongo.ReviewDAO;
import it.unipi.CarRev.dao.mongo.UserDAO;
import it.unipi.CarRev.dto.ReviewResponseDTO;
import it.unipi.CarRev.dto.UserReviewResponseDTO;
import it.unipi.CarRev.model.Review;
import it.unipi.CarRev.model.User;
import it.unipi.CarRev.service.exception.ResourceNotFoundException;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ShowReviewOfAUserServiceImplementation {
    private static final Integer PAGESIZE=10;
    private final UserDAO userDAO;
    private final ReviewDAO reviewDAO;

    public ShowReviewOfAUserServiceImplementation(UserDAO userDAO, ReviewDAO reviewDAO){
        this.reviewDAO=reviewDAO;
        this.userDAO=userDAO;
    }

    public Page<UserReviewResponseDTO> getAllReviewsFromUsers(String username, Integer numPage){
        User user=userDAO.findByUsername(username).orElse(null);
        if(user==null){
            throw new ResourceNotFoundException("cannot find this user");
        }
        List<UserReviewResponseDTO> returnedList=new ArrayList<>();
        Integer embeddedReviewSize=(user.getReviews()!=null)?user.getReviews().size():0;
        Integer otherReviewSize=(user.getOtherReviews()!=null)?user.getOtherReviews().size():0;
        long totalSize=(long) embeddedReviewSize+otherReviewSize;
        if(numPage==0) {
            if (embeddedReviewSize > 0) {
                returnedList = user.getReviews().stream()
                        .map(this::mapDocumentToDTO).toList();
            }
        }
        //its implied that a single user rarely will have more than 10 reviews
        else{
            if(otherReviewSize==0){
                return new PageImpl<>(new ArrayList<>(), PageRequest.of(numPage,PAGESIZE),0);
            }
            int end = otherReviewSize-((numPage - 1)*PAGESIZE);
            if (end <= 0) {
                return new PageImpl<>(new ArrayList<>(), PageRequest.of(numPage, PAGESIZE), totalSize);
            }
            int start = Math.max(0,end-PAGESIZE);
            List<Document> pagedIds=user.getOtherReviews().subList(start,end).reversed();
            List<String> idListToFetch=pagedIds.stream()
                    .map(doc->doc.getObjectId("_id").toHexString()).toList();
            List<Review> reviewList=reviewDAO.findAllById(idListToFetch);
            Map<String, Review> reviewMap = reviewList.stream()
                    .collect(Collectors.toMap(Review::getId, r -> r));
            returnedList=pagedIds.stream()
                    .map(reviewDoc->{
                        String id=reviewDoc.getObjectId("_id").toHexString();
                        Review review=reviewMap.get(id);
                        UserReviewResponseDTO reviewResponseDTO=new UserReviewResponseDTO();
                        if(review!=null){
                            reviewResponseDTO.setId(id);
                            reviewResponseDTO.setCarName(review.getCarName());
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
        }
        return new PageImpl<>(returnedList, PageRequest.of(numPage, PAGESIZE), totalSize);
    }


    private UserReviewResponseDTO mapDocumentToDTO(Document doc) {
        UserReviewResponseDTO dto = new UserReviewResponseDTO();
        dto.setId(doc.getObjectId("_id").toString());
        dto.setText(doc.getString("text"));
        dto.setCarName(doc.getString("car_name"));
        dto.setLikes(doc.getInteger("likes"));
        dto.setRating(doc.getDouble("rating"));
        if(doc.getInteger("year")!=null){
            dto.setYear(doc.getInteger("year"));
        }
        if(doc.getDouble("mileage")!=null){
            dto.setMileage(doc.getDouble("mileage"));
        }
        return dto;
    }
}
