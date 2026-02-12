package it.unipi.CarRev.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewResponseDTO {
    private String id;
    private Double rating;
    private String username;
    private String text;
    private Integer likes;
    private Double mileage;
    private Integer year;
}
