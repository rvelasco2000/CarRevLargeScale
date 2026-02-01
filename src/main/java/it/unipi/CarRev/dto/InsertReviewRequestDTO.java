package it.unipi.CarRev.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class InsertReviewRequestDTO {
    @NotNull(message="this field cannot be null")
    private String carId;
    @NotNull(message="this field cannot be null")
    private String text;
    @Min(value=0, message="the score cannot be a negative number")
    @Max(value=5,message="the score must be between 0 and 5")
    @NotNull(message="this field cannot be null")
    private Double rating;

    //must include a check for
    private Integer year;
    private Double mileage;

}
