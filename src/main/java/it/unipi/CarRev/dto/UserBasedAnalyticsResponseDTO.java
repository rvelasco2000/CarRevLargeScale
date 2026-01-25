package it.unipi.CarRev.dto;

import lombok.Getter;
import lombok.Setter;

public class UserBasedAnalyticsResponseDTO {
    @Getter@Setter
    private Integer totalRegisteredUsers;
    @Getter@Setter
    private Integer totalUnregisteredVisitors;
    @Getter@Setter
    private double registeredUsersTrend;
    @Getter@Setter
    private double unregisteredVisitorsTrend;
    @Getter@Setter
    private String period;
}
