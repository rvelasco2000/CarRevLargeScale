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
        try {
            return neo4j.query("""
                            MERGE (u:User {username: toLower($username)})
                            WITH u
                            
                            MATCH (c:Car)
                            WHERE
                              c.mongo_id=$mongo_id
                            
                            
                            MERGE (u)-[v:HAS_VISITED]->(c)
                            SET v.lastSeen = datetime()
                             WITH u
                             MATCH (u)-[old:HAS_VISITED]->(:Car)
                             WITH u, old
                             ORDER BY old.lastSeen DESC
                             SKIP 5
                             DELETE old;
                            """)
                    .bind(username).to("username")
                    .bind(car.getId()).to("mongo_id")
                    .fetchAs(Long.class)
                    .mappedBy((ts, r) -> r.get("matched").asLong())
                    .one()
                    .orElse(0L);
        } catch (Exception e) {
            System.err.println("error during ne04j visit: " + e.getMessage());
            return -1;
        }
    }
}