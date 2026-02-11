package it.unipi.CarRev.service.Impl;

import it.unipi.CarRev.dto.ReviewResponseDTO;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
public class ShowMostLikedReviewsServiceImplementation {

    public ReviewResponseDTO getMostLikedReviews(String carId){
        ObjectId objCarId=new ObjectId(carId);


    }
}
