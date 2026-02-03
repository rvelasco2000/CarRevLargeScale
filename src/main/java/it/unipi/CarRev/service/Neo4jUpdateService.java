package it.unipi.CarRev.service;

import it.unipi.CarRev.model.Car;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class Neo4jUpdateService {

    public enum UpdateOutcome { OK, NOT_FOUND, ERROR }

    private final Driver driver;

    public Neo4jUpdateService(Driver driver) {
        this.driver = driver;
    }

    public UpdateOutcome updateCarByOldSuperKey(Car oldCar, Car newCar) {
        String cypher = """
       
        MATCH (c:Car)
        WHERE toLower(c.car_brand) = toLower($old_brand)
          AND toLower(c.car_model) = toLower($old_model)
          AND ( (c.production_year IS NULL AND $old_year IS NULL) OR c.production_year = $old_year )
          AND ( (c.engine_displacement IS NULL AND $old_disp IS NULL) OR c.engine_displacement = $old_disp )
          AND ( (c.fuel_type IS NULL AND $old_fuel IS NULL) OR toLower(c.fuel_type) = toLower($old_fuel) )
          AND ( (c.transmission_type IS NULL AND $old_trans IS NULL) OR toLower(c.transmission_type) = toLower($old_trans) )
        
        WITH c
        LIMIT 1
        
        
        SET c.car_name = coalesce($new_name, c.car_name),
            c.car_brand = coalesce(toLower($new_brand), c.car_brand),
            c.car_model = coalesce(toLower($new_model), c.car_model),
            c.body_type = coalesce(toLower($new_body), c.body_type),
            c.drive_wheels = coalesce(toLower($new_drive), c.drive_wheels),
            c.engine_displacement = coalesce($new_disp, c.engine_displacement),
            c.number_of_cylinders = coalesce($new_cyl, c.number_of_cylinders),
            c.transmission_type = coalesce(toLower($new_trans), c.transmission_type),
            c.horse_power = coalesce($new_hp, c.horse_power),
            c.fuel_type = coalesce(toLower($new_fuel), c.fuel_type),
            c.seat_capacity = coalesce($new_seats, c.seat_capacity),
            c.price_new = coalesce($new_price, c.price_new),
            c.production_year = coalesce($new_year, c.production_year)
        
      
        WITH c
        
        OPTIONAL MATCH (c)-[r:OF_BRAND|OF_MODEL|HAS_BODY_TYPE|HAS_DRIVE|HAS_TRANSMISSION|USES_FUEL|OF_YEAR]->()
        DELETE r
        
        WITH c
        FOREACH (_ IN CASE WHEN $new_brand IS NULL OR trim($new_brand) = "" THEN [] ELSE [1] END |
          MERGE (b:Brand {name: toLower($new_brand)})
          MERGE (c)-[:OF_BRAND]->(b)
        )
        FOREACH (_ IN CASE WHEN $new_model IS NULL OR trim($new_model) = "" OR $new_brand IS NULL OR trim($new_brand) = "" THEN [] ELSE [1] END |
          MERGE (m:Model {name: toLower($new_model), brand: toLower($new_brand)})
          MERGE (c)-[:OF_MODEL]->(m)
        )
        FOREACH (_ IN CASE WHEN $new_body IS NULL OR trim($new_body) = "" THEN [] ELSE [1] END |
          MERGE (bt:BodyType {name: toLower($new_body)})
          MERGE (c)-[:HAS_BODY_TYPE]->(bt)
        )
        FOREACH (_ IN CASE WHEN $new_drive IS NULL OR trim($new_drive) = "" THEN [] ELSE [1] END |
          MERGE (dw:DriveWheels {name: toLower($new_drive)})
          MERGE (c)-[:HAS_DRIVE]->(dw)
        )
        FOREACH (_ IN CASE WHEN $new_trans IS NULL OR trim($new_trans) = "" THEN [] ELSE [1] END |
          MERGE (tr:Transmission {name: toLower($new_trans)})
          MERGE (c)-[:HAS_TRANSMISSION]->(tr)
        )
        FOREACH (_ IN CASE WHEN $new_fuel IS NULL OR trim($new_fuel) = "" THEN [] ELSE [1] END |
          MERGE (ft:FuelType {name: toLower($new_fuel)})
          MERGE (c)-[:USES_FUEL]->(ft)
        )
        FOREACH (_ IN CASE WHEN $new_year IS NULL THEN [] ELSE [1] END |
          MERGE (y:Year {value: $new_year})
          MERGE (c)-[:OF_YEAR]->(y)
        )
        
        
                WITH c
        
                OPTIONAL MATCH (c)-[rp:HAS_PRICE_BIN]->(:PriceBin)
                DELETE rp
                WITH c
                OPTIONAL MATCH (c)-[rd:HAS_DISPLACEMENT_BIN]->(:DispBin)
                DELETE rd
                WITH c
        
                // PriceBin
                OPTIONAL MATCH (pb:PriceBin)
                WHERE c.price_new IS NOT NULL
                  AND c.price_new >= pb.min AND c.price_new < pb.max
                FOREACH (_ IN CASE WHEN pb IS NULL THEN [] ELSE [1] END |
                  MERGE (c)-[:HAS_PRICE_BIN]->(pb)
                )
        
                WITH c
        
                // DispBin
                OPTIONAL MATCH (db:DispBin)
                WHERE c.engine_displacement IS NOT NULL
                  AND c.engine_displacement >= db.min AND c.engine_displacement < db.max
                FOREACH (_ IN CASE WHEN db IS NULL THEN [] ELSE [1] END |
                  MERGE (c)-[:HAS_DISPLACEMENT_BIN]->(db)
                )
        
                RETURN 1 AS ok
        """;

        Map<String, Object> params = buildParams(oldCar, newCar);

        try (Session session = driver.session()) {
            var result = session.run(cypher, params);
            if (!result.hasNext()) return UpdateOutcome.NOT_FOUND;
            return UpdateOutcome.OK;
        } catch (Exception e) {
            System.out.println("Neo4j update error: " + e.getMessage());
            return UpdateOutcome.ERROR;
        }
    }


    public UpdateOutcome rollbackToOld(Car oldSnapshot, Car newSnapshot) {
        return updateCarByOldSuperKey(newSnapshot, oldSnapshot);
    }

    private Map<String, Object> buildParams(Car oldCar, Car newCar) {
        Map<String, Object> p = new HashMap<>();

        // OLD key
        p.put("old_brand", oldCar.getCarBrand());
        p.put("old_model", oldCar.getCarModel());
        p.put("old_year", oldCar.getProduction_year());
        p.put("old_disp", oldCar.getEngineDisplacement());
        p.put("old_fuel", oldCar.getFuelType());
        p.put("old_trans", oldCar.getTransmissionType());

        // NEW values
        p.put("new_name", newCar.getCarName());
        p.put("new_brand", newCar.getCarBrand());
        p.put("new_model", newCar.getCarModel());
        p.put("new_body", newCar.getBodyType());
        p.put("new_drive", newCar.getDriveWheels());
        p.put("new_disp", newCar.getEngineDisplacement());
        p.put("new_cyl", newCar.getNumberOfCylinders());
        p.put("new_trans", newCar.getTransmissionType());
        p.put("new_hp", newCar.getHorsePower());
        p.put("new_fuel", newCar.getFuelType());
        p.put("new_seats", newCar.getSeatCapacity());
        p.put("new_price", newCar.getPriceNew());
        p.put("new_year", newCar.getProduction_year());

        return p;
    }
}