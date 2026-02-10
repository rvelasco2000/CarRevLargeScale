package it.unipi.CarRev.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CarAnalyticsBetweenDatesRequestDTO {
    @NotBlank
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "the date must be in the format YYYY-MM-dd")
    private String startDate;

    @NotBlank
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "the date must be in the format YYYY-MM-dd")
    private String endDate;
}
