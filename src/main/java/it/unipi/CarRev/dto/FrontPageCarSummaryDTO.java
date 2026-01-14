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

    public FrontPageCarSummaryDTO(String id,String carBrand, String carModel, Double generalRating, String fuelType, String bodyType) {
        this.id = id;
        this.carBrand = carBrand;
        this.carModel = carModel;
        this.generalRating = generalRating;
        this.fuelType = fuelType;
        this.bodyType = bodyType;
    }
}
