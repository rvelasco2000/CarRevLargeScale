package it.unipi.CarRev.controller;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.web.bind.annotation.*;
import it.unipi.CarRev.service.Neo4jRecommendationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test/neo4j")
public class Neo4jTestController {

    private final Driver driver;

    public Neo4jTestController(Driver driver) {
        this.driver = driver;
    }

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        try (Session session = driver.session()) {
            var rec = session.run("RETURN 1 AS ok").single();
            return Map.of("ok", rec.get("ok").asInt());
        }
    }

    @GetMapping("/car")
    public Object getCar(
            @RequestParam String brand,
            @RequestParam String model
    ) {
        String cypher = """
                MATCH (c:Car)
                WHERE toLower(c.car_brand) = toLower($brand)
                  AND toLower(c.car_model) = toLower($model)
                OPTIONAL MATCH (c)-[:HAS_BODY_TYPE]->(b:BodyType)
                OPTIONAL MATCH (c)-[:HAS_DRIVE]->(d:Drive)
                OPTIONAL MATCH (c)-[:HAS_TRANSMISSION]->(t:Transmission)
                OPTIONAL MATCH (c)-[:USES_FUEL]->(f:FuelType)
                RETURN
                    c.car_brand      AS brand,
                    c.car_model      AS model,
                    c.production_year AS year,
                    c.engine_displacement AS displacement,
                    c.horse_power    AS hp,
                    b.name           AS body_type,
                    d.name           AS drive,
                    t.name           AS transmission,
                    f.name           AS FuelType
                LIMIT 1
                """;

        try (var session = driver.session()) {
            return session.run(cypher, Map.of(
                    "brand", brand,
                    "model", model
            )).single().asMap();
        }
    }

}



