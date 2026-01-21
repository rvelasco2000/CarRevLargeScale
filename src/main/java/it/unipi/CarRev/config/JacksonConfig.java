package it.unipi.CarRev.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/***
 * this class is required because spring for some reason does not find the bean for ObjectMapper
 */
@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }
}
