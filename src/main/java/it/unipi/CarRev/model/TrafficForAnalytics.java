package it.unipi.CarRev.model;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "TrafficForAnalytics")
public class TrafficForAnalytics {
    @Id @Getter @Setter
    private String id;
    @Getter @Setter
    private Integer nOfLegitimateUsers;
    @Getter @Setter
    private Integer nOfSuspiciousUsers;
    @Getter @Setter
    private String date;

    public TrafficForAnalytics(Integer nOfLegitimateUsers, Integer nOfSuspiciousUsers, String date) {
        this.nOfLegitimateUsers = nOfLegitimateUsers;
        this.nOfSuspiciousUsers = nOfSuspiciousUsers;
        this.date = date;
    }
}
