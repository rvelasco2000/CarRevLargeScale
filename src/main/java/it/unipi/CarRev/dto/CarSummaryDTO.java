package it.unipi.CarRev.dto;

public class CarSummaryDTO {

    private String id;
    private String carName;
    private String carBrand;
    private String carModel;
    private String bodyType;
    private Double engineDisplacement;
    private Integer numberOfCylinders;
    private Long views;

    public CarSummaryDTO() {}

    public CarSummaryDTO(String id, String carName, String carBrand, String carModel,
                         String bodyType, Double engineDisplacement,
                         Integer numberOfCylinders, Long views) {
        this.id = id;
        this.carName = carName;
        this.carBrand = carBrand;
        this.carModel = carModel;
        this.bodyType = bodyType;
        this.engineDisplacement = engineDisplacement;
        this.numberOfCylinders = numberOfCylinders;
        this.views = views;
    }

    public String getId() { return id; }
    public String getCarName() { return carName; }
    public String getCarBrand() { return carBrand; }
    public String getCarModel() { return carModel; }
    public String getBodyType() { return bodyType; }
    public Double getEngineDisplacement() { return engineDisplacement; }
    public Integer getNumberOfCylinders() { return numberOfCylinders; }
    public Long getViews() { return views; }

    public void setId(String id) { this.id = id; }
    public void setCarName(String carName) { this.carName = carName; }
    public void setCarBrand(String carBrand) { this.carBrand = carBrand; }
    public void setCarModel(String carModel) { this.carModel = carModel; }
    public void setBodyType(String bodyType) { this.bodyType = bodyType; }
    public void setEngineDisplacement(Double engineDisplacement) { this.engineDisplacement = engineDisplacement; }
    public void setNumberOfCylinders(Integer numberOfCylinders) { this.numberOfCylinders = numberOfCylinders; }
    public void setViews(Long views) { this.views = views; }
}
