package it.unipi.CarRev.dto;


import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FrontPageCarSummaryDTO {
    private String id;
    private String carBrand;
    private String carModel;
    private Double generalRating;
    private String fuelType;
    private String bodyType;

    public FrontPageCarSummaryDTO(){};

    public FrontPageCarSummaryDTO(String carBrand, String id, String carModel, Double generalRating, String fuelType, String bodyType) {
        this.carBrand = carBrand;
        this.id = id;
        this.carModel = carModel;
        this.generalRating = generalRating;
        this.fuelType = fuelType;
        this.bodyType = bodyType;
    }
}
