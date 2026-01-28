package it.unipi.CarRev.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class InsertReviewRequestDTO {
    @NotNull(message="this field cannot be null")
    private String carId;
    @NotNull(message="this field cannot be null")
    private String userId;
    @NotNull(message="this field cannot be null")
    private String text;
    @NotNull(message="this field cannot be null")
    private Double rating;

    private Integer year;
    private Double mileage;

}
