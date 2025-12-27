package it.unipi.CarRev.service.Impl;
import it.unipi.CarRev.config.RedisConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

public class BotDetectionService {

    private static final int EXPIRESTATUSKEY=86400; /*the key will expire after a day*/
    private static final int EXPIRENOFVISITEDPAGES=1;
    private static final int TRESHOLD=100;
    public void checkForBot(String idUser){
        String statusKey="TrafficLog:"+idUser+":status";
        String nOfVisitedPagesKey="TrafficLog:"+idUser+":numberOfVisitedPages";
        try(Jedis jedis=RedisConfig.getJedis()){

            jedis.set(statusKey,"legitimate", SetParams.setParams().nx().ex(EXPIRESTATUSKEY));

            String nOfVisitedPagePresent= jedis.set(nOfVisitedPagesKey,"0", SetParams.setParams().nx().ex(EXPIRENOFVISITEDPAGES));
            Long currentValue=jedis.incr(nOfVisitedPagesKey);
            if(currentValue>=TRESHOLD){
                jedis.set(statusKey,"suspicius",SetParams.setParams().keepttl());
            }










        }
    }
}
