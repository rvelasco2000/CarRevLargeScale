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

import java.util.List;
import java.util.Optional;
import java.util.Set;

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
            //if the car is not found it is possible that the admin has deleted it but it has remained in
            //the recent car list in redis. To avoid costly search into every list for each user
            //we have decided to check this only if trying visit a car fails.
            //In this case we can search in the user list and delete it from there.
            Authentication notFoundAuth=SecurityContextHolder.getContext().getAuthentication();
            if(notFoundAuth != null && notFoundAuth.isAuthenticated() && !(notFoundAuth instanceof AnonymousAuthenticationToken)){
                deleteRecent(id,notFoundAuth.getName());
            }
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
    protected void deleteRecent(String carId,String userId){
        String key="User:"+userId+":recentCar";
        final String CARSEARCH="\"id\":\""+carId+"\"";
        try(Jedis jedis=RedisConfig.getJedis()){

            List<String> currentList=jedis.zrange(key,0,-1);
            for(String listElem: currentList){
                if(listElem.contains(CARSEARCH)){
                    jedis.zrem(key,listElem);
                }
            }

        }
        catch(Exception e){
            System.err.print("An error has occurred during the delete of a recently viewed car:"+e.getMessage());
        }
    }
    @Async
    protected void saveRecentlyViewed(Car car,String userId){
        String key="User:"+userId+":recentCar";
        final int EXPIRE=86400;
        final int MAXELEM=5;
        final String CARSEARCH="\"id\":\""+car.getId()+"\"";
        try(Jedis jedis=RedisConfig.getJedis()){
            List<String> currentList=jedis.zrange(key,0,-1);
            for(String listElem: currentList){
                if(listElem.contains(CARSEARCH)){
                    jedis.zrem(key,listElem);
                }
            }
            FrontPageCarSummaryDTO summary=new FrontPageCarSummaryDTO(
                    car.getId(),
                    car.getCarBrand(),
                    car.getCarModel(),
                    car.getGeneralRating(),
                    car.getFuelType(),
                    car.getBodyType()
            );
            String json=objectMapper.writeValueAsString(summary);
            /*
            //eliminate eventual duplicate in the list
            jedis.lrem(key,0,json);
            jedis.lpush(key,json);
            jedis.ltrim(key,0,4);
            jedis.expire(key,EXPIRE);
             */
            double score=System.currentTimeMillis();
            jedis.zadd(key,score,json);

            long currentCar=jedis.zcard(key);
            if(currentCar>MAXELEM){
                jedis.zremrangeByRank(key,0,(int) (currentCar-MAXELEM)-1);
            }
            jedis.expire(key,EXPIRE);

        }
        catch (Exception e){
            System.out.println("error during the memorization of recently viewed car:"+e.getMessage());
        }
    }
}
