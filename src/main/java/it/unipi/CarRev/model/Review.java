package it.unipi.CarRev.model;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Getter@Setter
@Document(collection="reviews")
public class Review {
    @Id
    private String id;
    @Field("car_name")
    private String carName;
    private String username;
    private String text;
    private Double rating;
    private LocalDateTime timestamp;
    private Integer likes;
    @Indexed(direction= IndexDirection.DESCENDING)
    private Integer report;
    private Integer year;
    private Double mileage;
}
