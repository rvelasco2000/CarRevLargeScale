package it.unipi.CarRev.service;

import it.unipi.CarRev.model.Car;
import org.neo4j.driver.Driver;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class Neo4jCarDeleteService {

    private final Driver driver;

    public Neo4jCarDeleteService(Driver driver) {
        this.driver = driver;
    }

    public int deleteCarProjection(Car car) {
        String cypher = """
            MATCH (c:Car)
            WHERE toLower(c.car_name) = toLower($car_name)
              AND toLower(c.car_brand) = toLower($car_brand)
              AND toLower(c.car_model) = toLower($car_model)
              AND c.production_year = $production_year
              AND c.engine_displacement = $engine_displacement
              AND toLower(c.fuel_type) = toLower($fuel_type)
              AND toLower(c.transmission_type) = toLower($transmission_type)
            DETACH DELETE c
            RETURN count(*) AS deleted
            """;

        try (var session = driver.session()) {
            System.out.println("DEBUG Neo4j delete params:"
                    + " name=" + car.getCarName()
                    + " brand=" + car.getCarBrand()
                    + " model=" + car.getCarModel()
                    + " year=" + car.getProduction_year()
                    + " disp=" + car.getEngineDisplacement()
                    + " fuel=" + car.getFuelType()
                    + " trans=" + car.getTransmissionType());
            var rec = session.run(cypher, Map.of(
                    "car_name", car.getCarName(),
                    "car_brand", car.getCarBrand(),
                    "car_model", car.getCarModel(),
                    "production_year", car.getProduction_year(),
                    "engine_displacement", car.getEngineDisplacement(),
                    "fuel_type", car.getFuelType(),
                    "transmission_type", car.getTransmissionType()
            )).single();

            return rec.get("deleted").asInt();
        }
    }
}