package it.unipi.CarRev.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(collection = "CarAnalytics")
public class CarAnalytics {
    @Id
    @Getter @Setter
    private String id;

    @Getter @Setter
    @Indexed
    @Field("data")
    private String data;

    @Getter @Setter
    @Field("Cars_Info")
    private List<CarInfo> carsInfo;

    public CarAnalytics(String data, List<CarInfo> carsInfo) {
        this.data = data;
        this.carsInfo = carsInfo;
    }

    public CarAnalytics() {}

    public static class CarInfo {
        @Getter @Setter
        @Field("car_name")
        private String carName;

        @Getter @Setter
        @Field("car_brand")
        private String carBrand;

        @Getter @Setter
        @Field("car_model")
        private String carModel;

        @Getter @Setter
        @Field("car_id")
        private String carId;

        @Getter @Setter
        @Field("views")
        private Long views;

        @Getter @Setter
        @Field("num_reviews")
        private Integer numReviews;

        public CarInfo(String carName, String carBrand, String carModel, String carId, Long views, Integer numReviews) {
            this.carName = carName;
            this.carBrand = carBrand;
            this.carModel = carModel;
            this.carId = carId;
            this.views = views;
            this.numReviews = numReviews;
        }

        public CarInfo() {}
    }
}
