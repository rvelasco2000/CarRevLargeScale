package it.unipi.CarRev.config;

import org.neo4j.driver.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class Neo4jConfig {

    @Bean
    public Config customNeo4jConfig() {
        return Config.builder()
                .withConnectionTimeout(2, TimeUnit.SECONDS)
                .withMaxConnectionLifetime(30, TimeUnit.MINUTES)
                .build();
    }
}