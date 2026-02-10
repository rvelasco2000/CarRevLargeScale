package it.unipi.CarRev.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
//testing
@Document(collection = "cars")
@CompoundIndex(def="{'Top_Ten_Review._id':1}",name="index_for_embedded_reviews_in_car")
public class Car {

    @Id
    private String id;
   @Field("car_name")
    private String carName;
    @Field("car_brand")
    private String carBrand;
    @Field("car_model")
    private String carModel;
    @Field("body_type")
    private String bodyType;

    @Field("drive_wheels")
    private String driveWheels;

    @Field("engine_displacement")
    private double engineDisplacement;

    @Field("number_of_cylinders")
    private Integer numberOfCylinders;

    @Field("transmission_type")
    private String transmissionType;

    @Field("horse_power")
    private Integer horsePower;

    @Field("fuel_type")
    private String fuelType;

    @Field("seat_capacity")
    private Integer seatCapacity;

    @Field("price_new")
    private Double priceNew;

    @Field("general_rating")
    private Double generalRating;

    @Field("Top_Ten_Review")
    private List<org.bson.Document> topTenReview;

    @Field("Other_review")
    private List<ObjectId> otherReview;

    private List<String> sales;

    @Field("views")
    private Long views;

    @Field("Product_Year")
    private List<org.bson.Document> productYear;

    @Field("production_year")
    private Integer production_year;

    @Field("total_review_score")
    private Double totalScore;

    @Field("number_of_reviews")
    private Integer numberOfReviews;


    public Car(String carName, String carBrand, String carModel, String bodyType, String driveWheels, Double engineDisplacement, Integer numberOfCylinders, String transmissionType, Integer horsePower, String fuelType, Integer seatCapacity, Double priceNew, Double generalRating, List<org.bson.Document> topTenReview, List<ObjectId> otherReview, List<String> sales, Long views, List<org.bson.Document> productYear, Integer production_year, Double totalScore, Integer numberOfReviews){
        this.carName = carName;
        this.carBrand = carBrand;
        this.carModel = carModel;
        this.bodyType = bodyType;
        this.driveWheels = driveWheels;
        this.engineDisplacement = engineDisplacement;
        this.numberOfCylinders = numberOfCylinders;
        this.transmissionType = transmissionType;
        this.horsePower = horsePower;
        this.fuelType = fuelType;
        this.seatCapacity = seatCapacity;
        this.priceNew = priceNew;
        this.generalRating = generalRating;
        this.topTenReview = topTenReview;
        this.otherReview = otherReview;
        this.sales = sales;
        this.views = views;
        this.productYear = productYear;
        this.production_year = production_year;
        this.totalScore=totalScore;
        this.numberOfReviews=numberOfReviews;
    }

    public Car() {}

    public String getId() { return id; }
    public String getCarName() { return carName; }
    public String getCarBrand() { return carBrand; }
    public String getCarModel() { return carModel; }
    public String getBodyType() { return bodyType; }
    public String getDriveWheels() { return driveWheels; }
    public Double getEngineDisplacement() { return engineDisplacement; }
    public Integer getNumberOfCylinders() { return numberOfCylinders; }
    public String getTransmissionType() { return transmissionType; }
    public Integer getHorsePower() { return horsePower; }
    public String getFuelType() { return fuelType; }
    public Integer getSeatCapacity() { return seatCapacity; }
    public Double getPriceNew() { return priceNew; }
    public Double getGeneralRating() { return generalRating; }
    public List<org.bson.Document> getTopTenReview() { return topTenReview; }
    public List<ObjectId> getOtherReview() { return otherReview; }
    public List<String> getSales() { return sales; }
    public Long getViews() { return views; }
    public List<org.bson.Document> getProductYear() { return productYear; }
    public Integer getProduction_year() { return production_year; }
    public Double getTotalScore(){return totalScore;}
    public Integer getNumberOfReviews(){return numberOfReviews;}


    public void setId(String id) { this.id = id; }
    public void setCarName(String carName) { this.carName = carName; }
    public void setCarBrand(String carBrand) { this.carBrand = carBrand; }
    public void setCarModel(String carModel) { this.carModel = carModel; }
    public void setBodyType(String bodyType) { this.bodyType = bodyType; }
    public void setDriveWheels(String driveWheels) { this.driveWheels = driveWheels; }
    public void setEngineDisplacement(Double engineDisplacement) { this.engineDisplacement = engineDisplacement; }
    public void setNumberOfCylinders(Integer numberOfCylinders) { this.numberOfCylinders = numberOfCylinders; }
    public void setTransmissionType(String transmissionType) { this.transmissionType = transmissionType; }
    public void setHorsePower(Integer horsePower) { this.horsePower = horsePower; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }
    public void setSeatCapacity(Integer seatCapacity) { this.seatCapacity = seatCapacity; }
    public void setPriceNew(Double priceNew) { this.priceNew = priceNew; }
    public void setGeneralRating(Double generalRating) { this.generalRating = generalRating; }
    public void setTopTenReview(List<org.bson.Document> topTenReview) { this.topTenReview = topTenReview; }
    public void setOtherReview(List<ObjectId> otherReview) { this.otherReview = otherReview; }
    public void setSales(List<String> sales) { this.sales = sales; }
    public void setViews(Long views) { this.views = views; }
    public void setProduction_year(Integer production_year) { this.production_year = production_year; }
    public void setProductYear(List<org.bson.Document> productYear) { this.productYear = productYear; }
    public void setTotalScore(Double totalScore){this.totalScore=totalScore;}
    public void setNumberOfReviews(Integer numberOfReviews){this.numberOfReviews=numberOfReviews;}
}
