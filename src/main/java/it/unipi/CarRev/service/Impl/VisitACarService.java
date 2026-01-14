package it.unipi.CarRev.service.Impl;


import it.unipi.CarRev.config.RedisConfig;
import it.unipi.CarRev.dao.mongo.CarDAO;
import it.unipi.CarRev.dto.FrontPageCarSummaryDTO;
import it.unipi.CarRev.dto.FullCarInfoDTO;
import it.unipi.CarRev.model.Car;
import it.unipi.CarRev.utils.CarUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.Optional;

@Service
public class VisitACarService {
    @Autowired
    private CarDAO carDAO;
    @Autowired
    private CachedViewsForCarService cachedViewsForCarService;
    @Autowired
    private ObjectMapper objectMapper;

    public FullCarInfoDTO getCarById(String id){
        Car car=carDAO.findById(id).orElse(null);
        if(car==null){
            return null;
        }
        cachedViewsForCarService.updateCachedViewsForCar(id);
        //if the user is logged i need to save some info about the car into redis
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)){
            String username=auth.getName();
            saveRecentlyViewed(car,username);
        }
        return CarUtils.mapCarToDto(car);
    }
    @Async
    protected void saveRecentlyViewed(Car car,String userId){
        String key="User:"+userId+":recentCar";
        final int EXPIRE=86400;
        try(Jedis jedis=RedisConfig.getJedis()){
            FrontPageCarSummaryDTO summary=new FrontPageCarSummaryDTO(
                    car.getId(),
                    car.getCarBrand(),
                    car.getCarModel(),
                    car.getGeneralRating(),
                    car.getFuelType(),
                    car.getBodyType()
            );
            String json=objectMapper.writeValueAsString(summary);
            //eliminate eventual duplicate in the list
            jedis.lrem(key,0,json);
            jedis.lpush(key,json);
            jedis.ltrim(key,0,4);
            jedis.expire(key,EXPIRE);

        }


    }
}
