package it.unipi.CarRev.dto;

import lombok.Getter;
import lombok.Setter;

@Setter@Getter
public class TrafficInfoAnalyticsResultDTO {
    private String period;
    private Integer totalLegitimateUsersForPeriod;
    private Integer totalSuspiciousUsersForPeriod;
    private Double ratio;

}
