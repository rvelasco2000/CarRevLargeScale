package it.unipi.CarRev.service.Impl;

import it.unipi.CarRev.config.RedisConfig;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
@Service
public class CachedViewsForCarService {
    @Async
    public  void updateCachedViewsForCar(String id){
        String carKey="Car:"+id+":views";

        try(Jedis jedis= RedisConfig.getJedis()){
            jedis.incr(carKey);
        }
        catch (Exception e){
            System.err.println("Error during the caching mechanism for views: "+e.getMessage());
        }

    }
}
