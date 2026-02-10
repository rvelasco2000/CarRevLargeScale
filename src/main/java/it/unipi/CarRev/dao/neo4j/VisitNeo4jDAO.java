package it.unipi.CarRev.dao.neo4j;

import it.unipi.CarRev.model.Car;

public interface VisitNeo4jDAO {
    long mergeVisited(String username, Car car);
}