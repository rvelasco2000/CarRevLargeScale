package it.unipi.CarRev.service.Impl;


import it.unipi.CarRev.config.RedisConfig;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
public class CachedLikeAReviewServiceImplementation {
    public void likeAReview(String reviewId){
        final String LIKEAREVIEWKEY="Reviews:"+reviewId+":numLikes";
        try(Jedis jedis= RedisConfig.getJedis()){
            jedis.incr(LIKEAREVIEWKEY);
        }
        catch(Exception e){
            System.out.println("Error during the insert of a like in redis");
        }
    }
}
