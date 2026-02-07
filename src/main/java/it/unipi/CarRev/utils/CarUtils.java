package it.unipi.CarRev.utils;

import it.unipi.CarRev.dto.FullCarInfoDTO;
import it.unipi.CarRev.model.Car;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

public class CarUtils {

    public static FullCarInfoDTO mapCarToDto(Car car){
        FullCarInfoDTO dto=new FullCarInfoDTO();
        dto.setId(car.getId());
        dto.setCarName(car.getCarName());
        dto.setCarBrand(car.getCarBrand());
        dto.setCarModel(car.getCarModel());
        dto.setBodyType(car.getBodyType());
        dto.setDriveWheels(car.getDriveWheels());
        dto.setEngineDisplacement(car.getEngineDisplacement());
        dto.setNumberOfCylinders(car.getNumberOfCylinders());
        dto.setTransmissionType(car.getTransmissionType());
        dto.setHorsePower(car.getHorsePower());
        dto.setFuelType(car.getFuelType());
        dto.setSeatCapacity(car.getSeatCapacity());
        dto.setPriceNew(car.getPriceNew());
        dto.setGeneralRating(car.getGeneralRating());
        //dto.setTopTenReview(car.getTopTenReview());
        if(car.getTopTenReview()!=null){
            List<Document> readableReviews=car.getTopTenReview().stream()
                    .map(doc->{
                        Document newDoc=new Document(doc);
                        if(newDoc.get("_id") instanceof ObjectId oid){
                            newDoc.put("_id",oid.toHexString());
                        }
                        return newDoc;
                    }).toList();
            dto.setTopTenReview(readableReviews);
        }
        if(car.getOtherReview()!=null){
            dto.setOtherReview(car.getOtherReview().stream().map(oid->oid.toHexString()).toList());
        }
        //dto.setOtherReview(car.getOtherReview());
        dto.setSales(car.getSales());
        dto.setViews(car.getViews());
        //dto.setProductYear(car.getProductYear());
        if(car.getProductYear()!=null){
            List<Document> readableYear=car.getProductYear().stream()
                    .map(doc->new Document()
                            .append("Year",doc.get("Year"))
                            .append("Average_Mileage",doc.get("Average_Mileage")))
                    .toList();
            dto.setProductYear(readableYear);
        }
        return dto;
    }
}
