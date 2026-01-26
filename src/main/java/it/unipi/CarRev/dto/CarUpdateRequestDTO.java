package it.unipi.CarRev.dto;

import jakarta.validation.constraints.NotNull;

public class CarUpdateRequestDTO {
    @NotNull(message="this field cannot be null")
    private String id;

    private String carName;
    private String carBrand;
    private String carModel;
    private String bodyType;
    private String driveWheels;
    private Integer engineDisplacement;
    private Integer numberOfCylinders;
    private String transmissionType;
    private Integer horsePower;
    private String fuelType;
    private Integer seatCapacity;
    private Double priceNew;
}
