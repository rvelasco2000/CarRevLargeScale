package it.unipi.CarRev.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CarAnalyticsTopCarDTO {
    private String carId;
    private String carName;
    private String carBrand;
    private String carModel;
    private Long viewsCount;
    private Integer reviewsCount;

    public CarAnalyticsTopCarDTO(String carId, String carName, String carBrand, String carModel,
                                 Long viewsCount, Integer reviewsCount) {
        this.carId = carId;
        this.carName = carName;
        this.carBrand = carBrand;
        this.carModel = carModel;
        this.viewsCount = viewsCount;
        this.reviewsCount = reviewsCount;
    }

    public CarAnalyticsTopCarDTO() {}
}
