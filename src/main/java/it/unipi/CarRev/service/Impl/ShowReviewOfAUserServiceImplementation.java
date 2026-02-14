package it.unipi.CarRev.service.Impl;


import it.unipi.CarRev.dao.mongo.ReviewDAO;
import it.unipi.CarRev.dao.mongo.UserDAO;
import it.unipi.CarRev.dto.ReviewResponseDTO;
import it.unipi.CarRev.dto.UserReviewResponseDTO;
import it.unipi.CarRev.model.User;
import it.unipi.CarRev.service.exception.ResourceNotFoundException;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShowReviewOfAUserServiceImplementation {
    private static final Integer PAGESIZE=10;
    private UserDAO userDAO;

    public ShowReviewOfAUserServiceImplementation(UserDAO userDAO){
        this.userDAO=userDAO;
    }

    public Page<UserReviewResponseDTO> getAllReviewsFromUsers(String userId, Integer numPage){
        ObjectId objectUserId=new ObjectId(userId);
        User user=userDAO.findById(userId).orElse(null);
        if(user==null){
            throw new ResourceNotFoundException("cannot find this user");
        }
        List<UserReviewResponseDTO> returnedList=new ArrayList<>();
        Integer embeddedReviewSize=(user.getReviews()!=null)?user.getReviews().size():0;
        Integer otherReviewSize=(user.getOtherReviews()!=null)?user.getOtherReviews().size():0;
        long totalSize=(long) embeddedReviewSize+otherReviewSize;
        if(numPage==0){
            if(embeddedReviewSize>0){
                returnedList=user.getReviews().stream()
                        .map(this::mapDocumentToDTO).toList();
            }
            else{
                if(otherReviewSize>0){
                    Integer start=(numPage-1)*PAGESIZE;
                    if(start<otherReviewSize){
                        int end=Math.min(start)
                    }
                }
            }
        }

    }
    private UserReviewResponseDTO mapDocumentToDTO(Document doc) {
        UserReviewResponseDTO dto = new UserReviewResponseDTO();
        dto.setId(doc.getObjectId("_id").toString());
        dto.setText(doc.getString("text"));
        dto.setCarName(doc.getString("carName"));
        dto.setLikes(doc.getInteger("likes"));
        dto.setRating(doc.getDouble("rating"));
        if(doc.getInteger("year")!=null){
            dto.setYear(doc.getInteger("yeae"));
        }
        if(doc.getDouble("mileage")!=null){
            dto.setMileage(doc.getDouble("mileage"));
        }
        return dto;
    }
}
