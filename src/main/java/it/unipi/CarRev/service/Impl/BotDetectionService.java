package it.unipi.CarRev.service.Impl;
import it.unipi.CarRev.config.RedisConfig;
import it.unipi.CarRev.utils.UtilsForDate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;
@Service
public class BotDetectionService {

    private static final int EXPIRESTATUSKEY=86400; /*the key will expire after a day*/
    private static final int EXPIRENOFVISITEDPAGES=1; /*change this if not working*/
    private static final int TRESHOLD=100;
    public Boolean checkForBot(String idUser){
        String statusKey="TrafficLog:"+idUser+":status";
        String nOfVisitedPagesKey="TrafficLog:"+idUser+":numberOfVisitedPages";
        String nOfLegitimateUserKey="TrafficLog:"+ UtilsForDate.getDate()+":legitimate";
        String nOfSuspiciusUserKey="TrafficLog:"+UtilsForDate.getDate()+":suspicious";
        try(Jedis jedis=RedisConfig.getJedis()){
            String currentStatus=jedis.get(statusKey);
            if(currentStatus!=null && currentStatus.equals("suspicious")){
                return false;
            }
            jedis.set(nOfLegitimateUserKey,"0",SetParams.setParams().nx());
            jedis.set(nOfSuspiciusUserKey,"0",SetParams.setParams().nx());
            String statusKeyCreated=jedis.set(statusKey,"legitimate", SetParams.setParams().nx().ex(EXPIRESTATUSKEY));
            if(statusKeyCreated!=null){
                jedis.incr(nOfLegitimateUserKey);
            }
            String nOfVisitedPagePresent= jedis.set(nOfVisitedPagesKey,"0", SetParams.setParams().nx().ex(EXPIRENOFVISITEDPAGES));
            Long currentValue=jedis.incr(nOfVisitedPagesKey);
            if(currentValue>TRESHOLD){
                jedis.set(statusKey,"suspicious",SetParams.setParams().keepttl());
                jedis.incr(nOfSuspiciusUserKey);
                jedis.decr(nOfLegitimateUserKey);
                return false;
            }
            return true;
        }
        catch(Exception e){
            System.err.println("Error during the creation of redis key for bot detection:"+e.getMessage());
            return true;
        }
    }
    public void mapIpToUsername(String username,String ip){
        String oldStatusKey="TrafficLog:"+ip+":status";
        String oldNOfVisitedPagesKey="TrafficLog:"+ip+":numberOfVisitedPages";
        String newStatusKey="TrafficLog:"+username+":status";
        String newNOfVisitedPagesKey="TrafficLog:"+username+":numberOfVisitedPages";
        try(Jedis jedis=RedisConfig.getJedis()){
            if(jedis.exists(oldStatusKey)){
                jedis.rename(oldStatusKey,newStatusKey);
            }
            if(jedis.exists(oldNOfVisitedPagesKey)){
                jedis.rename(oldNOfVisitedPagesKey,newNOfVisitedPagesKey);
            }
        }
        catch(Exception e){
            System.err.println("error during the renaming of the redis key:"+e.getMessage());
        }
    }
}
