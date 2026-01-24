package it.unipi.CarRev.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "sales")
public class Sale {

    @Id
    private String id;

    @Field("year")
    private Integer year;

    @Field("mileage")
    private Integer mileage;

    @Field("price")
    private Double price;

    public Sale() {}

    public String getId() { return id; }
    public Integer getYear() { return year; }
    public Integer getMileage() { return mileage; }
    public Double getPrice() { return price; }

    public void setId(String id) { this.id = id; }
    public void setYear(Integer year) { this.year = year; }
    public void setMileage(Integer mileage) { this.mileage = mileage; }
    public void setPrice(Double price) { this.price = price; }
}
