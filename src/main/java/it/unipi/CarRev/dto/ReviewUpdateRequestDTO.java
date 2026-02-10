package it.unipi.CarRev.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class ReviewUpdateRequestDTO {
    @NotNull(message = "this field cannot be null")
    private String id;

    private String text;
    @Min(value=0, message="the score cannot be a negative number")
    @Max(value=5,message="the score must be between 0 and 5")
    private Double rating;

    private Integer year;
    private Double mileage;
}
