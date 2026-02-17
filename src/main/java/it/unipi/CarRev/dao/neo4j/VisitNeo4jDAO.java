package it.unipi.CarRev.dao.neo4j;

import it.unipi.CarRev.model.Car;

public interface VisitNeo4jDAO {
    void mergeVisited(String username, Car car);
}