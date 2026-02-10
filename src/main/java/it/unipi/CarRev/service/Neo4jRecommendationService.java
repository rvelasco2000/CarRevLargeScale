package it.unipi.CarRev.service;

import org.neo4j.driver.*;
import org.springframework.stereotype.Service;

import java.util.*;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Neo4jRecommendationService {

    private final Driver driver;

    public Neo4jRecommendationService(Driver driver) {
        this.driver = driver;
    }


    private static final double PRICE_PCT = 0.20;
    private static final double DISP_DELTA = 0.4;
    private static final int K = 5;


    private static final String RECOMMENDATION_QUERY = """
        MATCH (u:User {username: $username})-[:HAS_VISITED]->(click:Car)
        WITH DISTINCT click

        MATCH (seed:Car {mongo_id: click.mongo_id})

        MATCH (seed)-[:HAS_BODY_TYPE|:HAS_DRIVE|:HAS_TRANSMISSION|:USES_FUEL|:OF_BRAND|:OF_MODEL|:HAS_PRICE_BIN|:HAS_DISPLACEMENT_BIN]->(feat)
        MATCH (cand:Car)-[:HAS_BODY_TYPE|:HAS_DRIVE|:HAS_TRANSMISSION|:USES_FUEL|:OF_BRAND|:OF_MODEL|:HAS_PRICE_BIN|:HAS_DISPLACEMENT_BIN]->(feat)
        WHERE cand <> seed
          AND toLower(coalesce(cand.car_brand,'')) <> toLower(coalesce(seed.car_brand,''))

          AND (click.price_new IS NULL OR (cand.price_new IS NOT NULL AND
               cand.price_new >= click.price_new * (1 - $pricePct) AND
               cand.price_new <= click.price_new * (1 + $pricePct)))

          AND (click.engine_displacement IS NULL OR (cand.engine_displacement IS NOT NULL AND
               cand.engine_displacement >= click.engine_displacement - $dispDelta AND
               cand.engine_displacement <= click.engine_displacement + $dispDelta))

        WITH
          cand,
          count(DISTINCT feat) AS shared_features,
          avg(CASE WHEN click.price_new IS NULL OR cand.price_new IS NULL THEN NULL ELSE abs(cand.price_new - click.price_new) END) AS avg_price_diff,
          avg(CASE WHEN click.engine_displacement IS NULL OR cand.engine_displacement IS NULL THEN NULL ELSE abs(cand.engine_displacement - click.engine_displacement) END) AS avg_disp_diff

        ORDER BY shared_features DESC, avg_price_diff ASC, avg_disp_diff ASC

        WITH
          cand.car_brand AS brand,
          cand.car_model AS model,
          head(collect(cand)) AS rep,
          head(collect(shared_features)) AS shared_features,
          head(collect(avg_price_diff)) AS avg_price_diff,
          head(collect(avg_disp_diff)) AS avg_disp_diff

        RETURN
          rep.car_name             AS car_name,
          rep.car_brand            AS car_brand,
          rep.car_model            AS car_model,
          rep.mongo_id             AS mongo_id,
          rep.production_year      AS production_year,
          rep.price_new            AS price_new,
          rep.engine_displacement  AS engine_displacement,
          shared_features          AS shared_features,
          avg_price_diff           AS avg_price_diff,
          avg_disp_diff            AS avg_disp_diff
        ORDER BY shared_features DESC, avg_price_diff ASC, avg_disp_diff ASC
        LIMIT $k
        """;


    public  List<Map<String, Object>> recommendForUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            return List.of();
        }

        Map<String, Object> params = new HashMap<>();
        params.put("username", username.trim());
        params.put("pricePct", PRICE_PCT);
        params.put("dispDelta", DISP_DELTA);
        params.put("k", K);

        try (Session session = driver.session()) {
            Result result = session.run(RECOMMENDATION_QUERY, params);
            return result.list(r -> r.asMap());
        }
    }
}