package it.unipi.CarRev.service;

import it.unipi.CarRev.model.Car;
import org.neo4j.driver.Driver;
import org.springframework.stereotype.Service;
import it.unipi.CarRev.model.Car;

import java.util.Map;

@Service
public class Neo4jCarDeleteService {

    private final Driver driver;

    public Neo4jCarDeleteService(Driver driver) {
        this.driver = driver;
    }

    public int deleteCarProjection(String id) {
        String cypher = """
                MATCH (c:Car {mongo_id: $id})
                        DETACH DELETE c
                        RETURN count(c) AS deleted
            """;

        try (var session = driver.session()) {
            System.out.println("DEBUG Neo4j delete params: mongo_id=" + id);

            var rec = session.run(cypher, Map.of(
                    "id", id
            )).single();

            int deleted = rec.get("deleted").asInt();
            return deleted;
        }}}