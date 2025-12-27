package it.unipi.CarRev.service.Impl;
import it.unipi.CarRev.config.RedisConfig;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;
@Service
public class BotDetectionService {

    private static final int EXPIRESTATUSKEY=86400; /*the key will expire after a day*/
    private static final int EXPIRENOFVISITEDPAGES=1; /*change this if not workig*/
    private static final int TRESHOLD=100;
    public Boolean checkForBot(String idUser){
        String statusKey="TrafficLog:"+idUser+":status";
        String nOfVisitedPagesKey="TrafficLog:"+idUser+":numberOfVisitedPages";
        try(Jedis jedis=RedisConfig.getJedis()){
            String currentStatus=jedis.get(statusKey);
            if(currentStatus!=null && currentStatus.equals("suspicious")){
                return false;
            }
            jedis.set(statusKey,"legitimate", SetParams.setParams().nx().ex(EXPIRESTATUSKEY));

            String nOfVisitedPagePresent= jedis.set(nOfVisitedPagesKey,"0", SetParams.setParams().nx().ex(EXPIRENOFVISITEDPAGES));
            Long currentValue=jedis.incr(nOfVisitedPagesKey);
            if(currentValue>TRESHOLD){
                jedis.set(statusKey,"suspicious",SetParams.setParams().keepttl());
                return false;
            }
            return true;
        }
    }
}
