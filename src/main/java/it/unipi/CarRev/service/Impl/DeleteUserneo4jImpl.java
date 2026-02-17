package it.unipi.CarRev.service.Impl;
import it.unipi.CarRev.dao.neo4j.DeleteUserNeo4jDAO;
import org.neo4j.driver.Driver;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DeleteUserneo4jImpl implements DeleteUserNeo4jDAO {

    private final Driver driver;

    public DeleteUserneo4jImpl(Driver driver) {
        this.driver = driver;
    }

 String cypher= """
         MATCH(u:User{username:$username})
         WITH u,count(u) AS returned
         DETACH DELETE u
         RETURN returned;
         """;


    @Override

    public long deleteUser(String username) {
        var result = driver.executableQuery(cypher)
                .withParameters(Map.of("username", username))
                .execute();

        var records = result.records();
        if (records.isEmpty()) return 0L;

        var v = records.get(0).get("returned");
        if (v == null || v.isNull()) return 0L;

        return v.asLong();}}