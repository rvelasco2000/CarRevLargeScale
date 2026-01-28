package it.unipi.CarRev.service.Impl;

import it.unipi.CarRev.config.RedisConfig;
import it.unipi.CarRev.dto.FrontPageCarSummaryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import redis.clients.jedis.Jedis;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class LastFiveCarServiceImplementation {

    @Autowired
    private final ObjectMapper objectMapper;
    public LastFiveCarServiceImplementation(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<FrontPageCarSummaryDTO> getLastFiveCar(){
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)){
            String username=auth.getName();
            return getCars(username);
        }
        else{
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"you must log in to see the last 5 visited cars");
        }
    }
    private List<FrontPageCarSummaryDTO> getCars(String username){
        String key="User:"+username+":recentCar";
        List<FrontPageCarSummaryDTO> lastCars=new ArrayList<>();
        try(Jedis jedis= RedisConfig.getJedis()){
            List<String> jsonList=jedis.zrevrange(key,0,4);
            if(jsonList!=null){
                for(String jsonElem:jsonList){
                    try{
                        FrontPageCarSummaryDTO car=objectMapper.readValue(jsonElem,FrontPageCarSummaryDTO.class);
                        lastCars.add(car);
                    }
                    catch (Exception e){
                        System.err.println("Error during the json parsing for last 5 visited cars:"+e.getMessage());
                    }
                }
            }

        }
        catch(Exception e){
            System.err.println("Error during redis retrieval of the last 5 visited car:"+e.getMessage());
        }
        return lastCars;
    }
}
