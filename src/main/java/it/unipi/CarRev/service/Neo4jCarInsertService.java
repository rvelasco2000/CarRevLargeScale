package it.unipi.CarRev.service;

import it.unipi.CarRev.dto.CarCreateRequestDTO;
import it.unipi.CarRev.model.Car;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
//test
@Service
@Async
public class Neo4jCarInsertService {

    private final Driver driver;

    public Neo4jCarInsertService(Driver driver) {
        this.driver = driver;
    }

    public void insertCar(Car car) {


        String cypher = """
                CREATE (c:Car)
                         SET
                           c.mongo_id = $mongo_id,
                           c.car_name = $car_name,
                           c.car_brand = toLower($car_brand),
                           c.car_model = toLower($car_model),
                           c.production_year = $production_year,
                           c.body_type = toLower($body_type),
                           c.drive_wheels = toLower($drive_wheels),
                           c.engine_displacement = $engine_displacement,
                           c.number_of_cylinders = $number_of_cylinders,
                           c.transmission_type = toLower($transmission_type),
                           c.horse_power = $horse_power,
                           c.fuel_type = toLower($fuel_type),
                           c.seat_capacity = $seat_capacity,
                           c.price_new = $price_new
                         WITH c
                
                         MERGE (b:Brand {name: toLower($car_brand)})
                         MERGE (m:Model {name: toLower($car_model), brand: toLower($car_brand)})
                         MERGE (bt:BodyType {name: toLower($body_type)})
                         MERGE (dw:DriveWheels {name: toLower($drive_wheels)})
                         MERGE (tr:Transmission {name: toLower($transmission_type)})
                         MERGE (ft:FuelType {name: toLower($fuel_type)})
                
                         MERGE (c)-[:OF_BRAND]->(b)
                         MERGE (c)-[:OF_MODEL]->(m)
                         MERGE (c)-[:HAS_BODY_TYPE]->(bt)
                         MERGE (c)-[:HAS_DRIVE]->(dw)
                         MERGE (c)-[:HAS_TRANSMISSION]->(tr)
                         MERGE (c)-[:USES_FUEL]->(ft)
                
                         FOREACH (_ IN CASE WHEN $production_year IS NULL THEN [] ELSE [1] END |
                           MERGE (y:Year {value: $production_year})
                           MERGE (c)-[:OF_YEAR]->(y)
                         )
                
                         WITH c
                
                         // ---------- Displacement bin ----------
                         CALL (c) {
                           WITH c
                           MATCH (db:DispBin)
                           WHERE c.engine_displacement IS NOT NULL
                             AND c.engine_displacement >= db.min
                             AND c.engine_displacement <  db.max
                           WITH db
                           ORDER BY db.min DESC
                           LIMIT 1
                           MERGE (c)-[:HAS_DISPLACEMENT_BIN]->(db)
                           RETURN 1 AS d
                         }
                
                         // ---------- Price bin ----------
                         CALL (c) {
                           WITH c
                           MATCH (pb:PriceBin)
                           WHERE c.price_new IS NOT NULL
                             AND c.price_new >= pb.min
                             AND c.price_new <  pb.max
                           WITH pb
                           ORDER BY pb.min DESC
                           LIMIT 1
                           MERGE (c)-[:HAS_PRICE_BIN]->(pb)
                           RETURN 1  AS b
                         }
                
                         RETURN c.mongo_id AS mongo_id;
        """;

        Map<String, Object> p = new HashMap<>();
        p.put("mongo_id", car.getId());
        p.put("car_name", car.getCarName());
        p.put("car_brand", car.getCarBrand());
        p.put("car_model", car.getCarModel());
        p.put("production_year", car.getProduction_year());
        p.put("body_type", car.getBodyType());
        p.put("drive_wheels", car.getDriveWheels());
        p.put("engine_displacement", car.getEngineDisplacement());
        p.put("number_of_cylinders", car.getNumberOfCylinders());
        p.put("transmission_type", car.getTransmissionType());
        p.put("horse_power", car.getHorsePower());
        p.put("fuel_type", car.getFuelType());
        p.put("seat_capacity", car.getSeatCapacity());
        p.put("price_new", car.getPriceNew());

        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run(cypher, p);
                return null;
            });
        }
    }
}