package it.unipi.CarRev.dto;

import it.unipi.CarRev.Enum.DateEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class TrafficInfoAnalyticsRequestDTO {
    @NotBlank
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "the date must be in the format YYYY-MM-dd")
    private String startDate;
    @NotBlank
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "the date must be in the format YYYY-MM-dd")
    private String endDate;
    @NotNull(message="this field cannot be null")
    private DateEnum period;
}
