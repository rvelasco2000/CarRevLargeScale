package it.unipi.CarRev.service;

import org.neo4j.driver.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class Neo4jRecommendationService {

    private final Driver driver;

    public Neo4jRecommendationService(Driver driver) {
        this.driver = driver;
    }

    // Fixed parameters (your choice)
    private static final int SEED_PER_CLICK = 3;
    private static final double PRICE_PCT = 0.20;
    private static final double DISP_DELTA = 0.4;
    private static final int K = 5; // change to 20 if you want 20 results


    private static final String RECOMMENDATION_QUERY = """
        UNWIND $clicks AS click

        MATCH (seed:Car)
        WHERE
          toLower(seed.car_brand) = toLower(click.car_brand) AND
          toLower(seed.car_model) = toLower(click.car_model) AND
          (click.production_year IS NULL OR seed.production_year = click.production_year) AND
          (click.fuel_type IS NULL OR toLower(seed.fuel_type) = toLower(click.fuel_type)) AND
          (click.transmission_type IS NULL OR toLower(seed.transmission_type) = toLower(click.transmission_type)) AND
          (click.body_type IS NULL OR toLower(seed.body_type) = toLower(click.body_type)) AND
          (click.drive_wheels IS NULL OR toLower(seed.drive_wheels) = toLower(click.drive_wheels)) AND
          (click.engine_displacement IS NULL OR
             (seed.engine_displacement IS NOT NULL AND abs(seed.engine_displacement - click.engine_displacement) <= 0.05))

        WITH click, seed
        LIMIT $seedPerClick

        MATCH (seed)-[:HAS_BODY_TYPE|:HAS_DRIVE|:HAS_TRANSMISSION|:USES_FUEL|:OF_BRAND|:OF_MODEL|:HAS_PRICE_BIN|:HAS_DISPLACEMENT_BIN]->(feat)
        MATCH (cand:Car)-[:HAS_BODY_TYPE|:HAS_DRIVE|:HAS_TRANSMISSION|:USES_FUEL|:OF_BRAND|:OF_MODEL|:HAS_PRICE_BIN|:HAS_DISPLACEMENT_BIN]->(feat)
        WHERE cand <> seed
          AND toLower(cand.car_brand) <> toLower(seed.car_brand)

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
          rep.car_name        AS car_name,
          rep.car_brand       AS car_brand,
          rep.car_model       AS car_model,
          rep.production_year AS production_year,
          rep.price_new       AS price_new,
          rep.engine_displacement AS engine_displacement,
          shared_features     AS shared_features,
          avg_price_diff      AS avg_price_diff,
          avg_disp_diff       AS avg_disp_diff
        ORDER BY shared_features DESC, avg_price_diff ASC, avg_disp_diff ASC
        LIMIT $k
        """;

    /**
     * Only dynamic parameter is "clicks" (from Redis).
     * All other parameters are fixed constants inside the service.
     */
    public List<Map<String, Object>> recommendFromClicks(List<Map<String, Object>> clicks) {
        if (clicks == null || clicks.isEmpty()) {
            return List.of();
        }


        List<Map<String, Object>> cleanedClicks = clicks.stream()
                .map(Neo4jRecommendationService::normalizeClickForCypher)
                .toList();

        Map<String, Object> params = new HashMap<>();
        params.put("clicks", cleanedClicks);
        params.put("seedPerClick", SEED_PER_CLICK);
        params.put("pricePct", PRICE_PCT);
        params.put("dispDelta", DISP_DELTA);
        params.put("k", K);

        try (Session session = driver.session()) {
            Result result = session.run(RECOMMENDATION_QUERY, params);
            return result.list(r -> r.asMap());
        }
    }


    private static Map<String, Object> normalizeClickForCypher(Map<String, Object> click) {
        Map<String, Object> c = new HashMap<>();
        c.put("car_brand", lowerOrNull(click.get("car_brand")));
        c.put("car_model", lowerOrNull(click.get("car_model")));
        c.put("production_year", toIntegerOrNull(click.get("production_year")));
        c.put("fuel_type", lowerOrNull(click.get("fuel_type")));
        c.put("transmission_type", lowerOrNull(click.get("transmission_type")));
        c.put("body_type", lowerOrNull(click.get("body_type")));
        c.put("drive_wheels", lowerOrNull(click.get("drive_wheels")));
        c.put("engine_displacement", toDoubleOrNull(click.get("engine_displacement")));
        c.put("price_new", toDoubleOrNull(click.get("price_new")));
        return c;
    }

    private static String lowerOrNull(Object v) {
        if (v == null) return null;
        String s = v.toString().trim();
        return s.isEmpty() ? null : s.toLowerCase(Locale.ROOT);
    }

    private static Integer toIntegerOrNull(Object v) {
        if (v == null) return null;
        if (v instanceof Integer i) return i;
        if (v instanceof Long l) return Math.toIntExact(l);
        if (v instanceof Number n) return n.intValue();
        try {
            String s = v.toString().trim();
            return s.isEmpty() ? null : Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static Double toDoubleOrNull(Object v) {
        if (v == null) return null;
        if (v instanceof Double d) return d;
        if (v instanceof Float f) return (double) f;
        if (v instanceof Number n) return n.doubleValue();
        try {
            String s = v.toString().trim();
            return s.isEmpty() ? null : Double.parseDouble(s);
        } catch (Exception e) {
            return null;
        }
    }
}

