package it.unipi.CarRev.dto;


import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class ReviewResponseDTO {
    private String id;
    private Double rating;
    private String username;
    private String text;
    private Integer likes;
    private Double mileage;
    private Integer year;
}
