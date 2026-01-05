package it.unipi.CarRev.dto;

import com.mongodb.annotations.Sealed;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter @Setter
public class FullCarInfoDTO {
    private String id;
    private String carName;
    private String carBrand;
    private String carModel;
    private String bodyType;
    private String driveWheels;
    private Integer engineDisplacement;
    private Integer numberOfCylinders;
    private String transmissionType;
    private Integer horsePower;
    private String fuelType;
    private Integer seatCapacity;
    private Double priceNew;
    private Double generalRating;
    private List<String> topTenReview;
    private List<String> otherReview;
    private List<String> sales;
    private Long views;
    private List<?> productYear;

    public FullCarInfoDTO() {
    }

    public FullCarInfoDTO(String id, String carName, String carBrand, String carModel, String bodyType, String driveWheels, Integer engineDisplacement, Integer numberOfCylinders, String transmissionType, Integer horsePower, String fuelType, Integer seatCapacity, Double priceNew, Double generalRating, List<String> topTenReview, List<String> otherReview, List<String> sales, Long views, List<?> productYear) {
        this.id = id;
        this.carName = carName;
        this.carBrand = carBrand;
        this.carModel = carModel;
        this.bodyType = bodyType;
        this.driveWheels = driveWheels;
        this.engineDisplacement = engineDisplacement;
        this.numberOfCylinders = numberOfCylinders;
        this.transmissionType = transmissionType;
        this.horsePower = horsePower;
        this.fuelType = fuelType;
        this.seatCapacity = seatCapacity;
        this.priceNew = priceNew;
        this.generalRating = generalRating;
        this.topTenReview = topTenReview;
        this.otherReview = otherReview;
        this.sales = sales;
        this.views = views;
        this.productYear = productYear;
    }
}


