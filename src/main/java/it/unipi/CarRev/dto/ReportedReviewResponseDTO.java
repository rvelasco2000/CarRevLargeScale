package it.unipi.CarRev.dto;

import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class ReportedReviewResponseDTO {
    private String reviewId;
    private String carName;
    private Integer nOfReports;
    private String text;
}
