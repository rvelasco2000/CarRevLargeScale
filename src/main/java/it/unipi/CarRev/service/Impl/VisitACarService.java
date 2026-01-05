package it.unipi.CarRev.service.Impl;


import it.unipi.CarRev.config.RedisConfig;
import it.unipi.CarRev.dao.mongo.CarDAO;
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

import java.util.Optional;

@Service
public class VisitACarService {
    @Autowired
    private CarDAO carDAO;
    @Autowired
    private CachedViewsForCarService cachedViewsForCarService;

    public FullCarInfoDTO getCarById(String id){
        Car car=carDAO.findById(id).orElse(null);
        if(car==null){
            return null;
        }
        cachedViewsForCarService.updateCachedViewsForCar(id);
        //if the user is logged i need to save some info about the car into redis
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)){
            //i will insert here the logic for redis save
        }
        return CarUtils.mapCarToDto(car);
    }
}
