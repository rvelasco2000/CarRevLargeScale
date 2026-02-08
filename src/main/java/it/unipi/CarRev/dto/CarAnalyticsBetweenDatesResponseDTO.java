package it.unipi.CarRev.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CarAnalyticsBetweenDatesResponseDTO {
    private String startDate;
    private String endDate;
    private CarAnalyticsTopCarDTO mostViewedCar;
    private CarAnalyticsTopCarDTO mostReviewedCar;

    public CarAnalyticsBetweenDatesResponseDTO(String startDate, String endDate,
                                               CarAnalyticsTopCarDTO mostViewedCar,
                                               CarAnalyticsTopCarDTO mostReviewedCar) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.mostViewedCar = mostViewedCar;
        this.mostReviewedCar = mostReviewedCar;
    }

    public CarAnalyticsBetweenDatesResponseDTO() {}
}
