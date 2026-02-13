package it.unipi.CarRev.service.Impl;


import it.unipi.CarRev.dto.ReviewResponseDTO;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class ShowReviewForACarInTimestampOrder {

    public Page<ReviewResponseDTO> getReviewsByTimestamp(String carId,Integer numPage){
        ObjectId objectCarId=new ObjectId(carId);

    }
}
