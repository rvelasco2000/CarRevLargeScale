package it.unipi.CarRev.dao.neo4j;

import it.unipi.CarRev.model.Car;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

@Repository
public class VisitNeo4jDAOImpl implements VisitNeo4jDAO {

    private final Neo4jClient neo4j;

    public VisitNeo4jDAOImpl(Neo4jClient neo4j) {
        this.neo4j = neo4j;
    }

    public long mergeVisited(String username, Car car) {
        return neo4j.query("""
MERGE (u:User {username: toLower($username)})
WITH u


OPTIONAL MATCH (db:DispBin)
WHERE $disp IS NOT NULL AND $disp >= db.min AND $disp < db.max

OPTIONAL MATCH (pb:PriceBin)
WHERE $price IS NOT NULL AND $price >= pb.min AND $price < pb.max

WITH u, db, pb

MATCH (c:Car)
WHERE
  c.car_name = $carName AND
  c.car_brand = toLower($carBrand) AND
  c.car_model = toLower($carModel) AND
  c.production_year = $year AND
  c.fuel_type = toLower($fuel) AND
  c.transmission_type = toLower($trans) AND 
  c.body_type = toLower($body) AND
  c.drive_wheels = toLower($drive)


  AND ( $disp  IS NULL OR (db IS NOT NULL AND (c)-[:HAS_DISPLACEMENT_BIN]->(db)) )
  AND ( $price IS NULL OR (pb IS NOT NULL AND (c)-[:HAS_PRICE_BIN]->(pb)) )

MERGE (u)-[v:HAS_VISITED]->(c)
SET v.lastSeen = datetime()
RETURN count(c) AS matched;
        """)
                .bind(username).to("username")
                        .bind(car.getCarName()).to("carName")
                        .bind(car.getCarBrand()).to("carBrand")
                .bind(car.getCarModel()).to("carModel")
                .bind(car.getProduction_year()).to("year")
                .bind(car.getPriceNew()).to("price")
                .bind(car.getEngineDisplacement()).to("disp")
                .bind(car.getFuelType()).to("fuel")
                .bind(car.getDriveWheels()).to("drive")
                .bind(car.getBodyType()).to("body")
                .bind(car.getTransmissionType()).to("trans")
                .fetchAs(Long.class)
                .mappedBy((ts, r) -> r.get("matched").asLong())
                .one()
                .orElse(0L);
    }
}