package it.unipi.CarRev.service.Impl;

import it.unipi.CarRev.config.RedisConfig;
import it.unipi.CarRev.dao.mongo.TrafficForAnalyticsDAO;
import it.unipi.CarRev.dao.mongo.UserBasedAnalyticsDAO;
import it.unipi.CarRev.model.TrafficForAnalytics;
import it.unipi.CarRev.model.UserBasedAnalytics;
import it.unipi.CarRev.utils.UtilsForDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisAccessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.regex.Pattern;

@Service
public class TrafficForInfoServiceImpl {
    @Autowired
    TrafficForAnalyticsDAO trafficForAnalyticsDAO;
    @Autowired
    UserBasedAnalyticsDAO userBasedAnalyticsDAO;

    @Scheduled(cron="0 0 0 * * *")
    public void dailyTrafficInfoTransfer(){
        String yesterdayDate= UtilsForDate.getYesterdayDate();
        //uncomment this to test the application
        //String yesterdayDate=UtilsForDate.getDate();
        String nOfLegitimateUserKey="TrafficLog:"+yesterdayDate+":legitimate";
        String nOfSuspiciousUserKey="TrafficLog:"+yesterdayDate+":suspicious";
        String nOfRegisteredUserKey="TrafficLog:"+UtilsForDate.getDate()+":nOfRegisteredUser";
        String nOfUnregisteredVisitorKey="TrafficLog:"+ UtilsForDate.getDate()+":numberOfUnregisteredUsers";
        try(Jedis jedis= RedisConfig.getJedis()){
            String nOfLegitimateUser=jedis.get(nOfLegitimateUserKey);
            if(nOfLegitimateUser==null){
                nOfLegitimateUser="0";
            }
            String nOfSuspiciousUser=jedis.get(nOfSuspiciousUserKey);
            if(nOfSuspiciousUser==null){
                nOfSuspiciousUser="0";
            }
            String nOfRegisteredUser=jedis.get(nOfRegisteredUserKey);
            if(nOfRegisteredUser==null){
                nOfRegisteredUser="0";
            }
            String nOfUnregisteredUsers=jedis.get(nOfUnregisteredVisitorKey);
            if(nOfUnregisteredUsers==null){
                nOfUnregisteredUsers="0";
            }

            TrafficForAnalytics trafficForAnalytics=new TrafficForAnalytics(Integer.valueOf(nOfLegitimateUser),Integer.valueOf(nOfSuspiciousUser),yesterdayDate);
            UserBasedAnalytics userBasedAnalytics=new UserBasedAnalytics(Integer.valueOf(nOfRegisteredUser),Integer.valueOf(nOfUnregisteredUsers),UtilsForDate.getDate());
            trafficForAnalyticsDAO.save(trafficForAnalytics);
            userBasedAnalyticsDAO.save(userBasedAnalytics);
            jedis.del(nOfLegitimateUserKey);
            jedis.del(nOfSuspiciousUserKey);
            jedis.del(nOfRegisteredUserKey);
            jedis.del(nOfUnregisteredVisitorKey);
            System.out.println("midnight migration between Redis and Mongodb has been successful");
        }
        catch(Exception e){
            System.err.println("Error during midnight migration between Redis and Mongodb:"+e.getMessage());
        }
    }

}
