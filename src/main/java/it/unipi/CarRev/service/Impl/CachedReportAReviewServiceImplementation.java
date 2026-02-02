package it.unipi.CarRev.service.Impl;

import it.unipi.CarRev.config.RedisConfig;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
public class CachedReportAReviewServiceImplementation {

    public void reportAReview(String reviewId){
        final String REVIEWKEY="Reviews:"+reviewId+":numReports";
        try(Jedis jedis=RedisConfig.getJedis()){
            jedis.incr(REVIEWKEY);
        }
        catch(Exception e){
            System.out.println("Error during the insert of reviews in redis");
        }
    }

}
