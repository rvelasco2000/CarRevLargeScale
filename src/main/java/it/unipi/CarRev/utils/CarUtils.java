package it.unipi.CarRev.utils;

import it.unipi.CarRev.dto.FullCarInfoDTO;
import it.unipi.CarRev.model.Car;

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
        dto.setTopTenReview(car.getTopTenReview());
        dto.setOtherReview(car.getOtherReview());
        dto.setSales(car.getSales());
        dto.setViews(car.getViews());
        dto.setProductYear(car.getProductYear());
        return dto;
    }
}
