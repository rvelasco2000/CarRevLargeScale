package it.unipi.CarRev.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;
@Getter@Setter
public class CarCreateRequestDTO {
    @NotNull(message="this field cannot be null")
    private String carName;
    @NotNull(message="this field cannot be null")
    private String carBrand;
    @NotNull(message="this field cannot be null")
    private String carModel;
    @NotNull(message="this field cannot be null")
    private String bodyType;
    @NotNull(message="this field cannot be null")
    private String driveWheels;
    @NotNull(message="this field cannot be null")
    private Double engineDisplacement;
    @NotNull(message="this field cannot be null")
    private Integer numberOfCylinders;
    @NotNull(message="this field cannot be null")
    private String transmissionType;
    @NotNull(message="this field cannot be null")
    private Integer horsePower;
    @NotNull(message="this field cannot be null")
    private String fuelType;
    @NotNull(message="this field cannot be null")
    private Integer seatCapacity;
    @NotNull(message="this field cannot be null")
    private Double priceNew;
    @NotNull(message="this field cannot be null")
    private Integer ProductionYear;

}
