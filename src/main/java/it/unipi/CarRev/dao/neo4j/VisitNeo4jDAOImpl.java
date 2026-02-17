package it.unipi.CarRev.dao.neo4j;

import it.unipi.CarRev.model.Car;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

@Repository
public class VisitNeo4jDAOImpl implements VisitNeo4jDAO {

    private final Neo4jClient neo4j;

    public VisitNeo4jDAOImpl(Neo4jClient neo4j) {
        this.neo4j = neo4j;
    }
@Async
    public void mergeVisited(String username, Car car) {
    try {
        neo4j.query("""
                        MERGE (u:User {username: toLower($username)})
                                    WITH u
                        
                                    MATCH (c:Car)
                                    WHERE c.mongo_id = $mongo_id
                        
                                    MERGE (u)-[v:HAS_VISITED]->(c)
                                    SET v.lastSeen = datetime()
                        
                                    WITH u, c
                                    MATCH (u)-[old:HAS_VISITED]->(:Car)
                                    WITH u, c, old
                                    ORDER BY old.lastSeen DESC
                                    SKIP 5
                                    DELETE old
                        
                                    RETURN count(c) AS matched
                        """)
                .bind(username).to("username")
                .bind(car.getId()).to("mongo_id")
                .run();

    } catch (Exception e) {
        System.err.println("error during neo4j visit: " + e.getMessage());
    }
}}

