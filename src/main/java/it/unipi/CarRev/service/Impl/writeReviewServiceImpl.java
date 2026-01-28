package it.unipi.CarRev.service.Impl;

import it.unipi.CarRev.dto.InsertReviewRequestDTO;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class writeReviewServiceImpl {
    public boolean writeReview(InsertReviewRequestDTO insertReviewRequestDTO){
        Document newReview=new Document()
                .append("Car_name",insertReviewRequestDTO.getCarId())
                .append("Rating",insertReviewRequestDTO.getRating())
                .append("Timestamp", LocalDateTime.now())
                .append("Likes",0)
                .append("Report",0);

        if(insertReviewRequestDTO.getYear()!=null){
            newReview.append("Year",insertReviewRequestDTO.getYear());
        }
        if(insertReviewRequestDTO.getMileage()!=null){
            newReview.append("Mileage",insertReviewRequestDTO.getMileage());
        }
        return true;
    }
}
