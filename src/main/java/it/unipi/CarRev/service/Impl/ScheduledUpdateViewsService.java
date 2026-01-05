package it.unipi.CarRev.service.Impl;

import io.lettuce.core.api.sync.RedisAclCommands;
import it.unipi.CarRev.config.RedisConfig;

import java.util.Scanner;
import java.util.Set;
import java.util.HashSet;

import it.unipi.CarRev.model.Car;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.resps.ScanResult;

@Service
public class ScheduledUpdateViewsService {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Scheduled(cron="0 */10 * * * *")
    public void updateMongoViews(){
        Set<String> allKeys=getKeys();
        if(allKeys.isEmpty()){
            return;
        }
        BulkOperations bulkOperations=mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Car.class);
        Boolean bulkHasInstruction=false;
        System.out.println(allKeys.stream().toList());
        try(Jedis jedis=RedisConfig.getJedis()){
            for(String keys:allKeys){
                String[] splitKeys=keys.split(":");
                String mongoCarId=splitKeys[1];
                Integer redisViews=Integer.valueOf(jedis.getSet(keys,"0"));
                if(redisViews==0){
                    continue;
                }
                Query query=new Query(Criteria.where("id").is(mongoCarId));
                Update update=new Update().inc("views",redisViews);
                bulkOperations.updateOne(query,update);
                bulkHasInstruction=true;
                System.out.println(redisViews);
            }
            if(bulkHasInstruction){
                bulkOperations.execute();
                System.out.println("migration of views to mongoDB successful");
            }
           for(String keys:allKeys){
               if("0".equals(jedis.get(keys))){
                   jedis.del(keys);
               }
           }
        }
        catch(Exception e){
            System.err.println("error during the migration to views to mongoDB:"+e.getMessage());
        }
    }

    private Set<String> getKeys(){
        Set<String> allKeys=new HashSet<>();
        String cursor= ScanParams.SCAN_POINTER_START;
        ScanParams scanParams=new ScanParams().match("Car:*:views").count(100);
        try(Jedis jedis=RedisConfig.getJedis()){
            while(true){
                ScanResult<String> scanResult=jedis.scan(cursor,scanParams);
                allKeys.addAll(scanResult.getResult());
                cursor=scanResult.getCursor();
                if("0".equals(cursor)){
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("Error during fetch of the keys "+e.getMessage());
        }
        return allKeys;
    }
}
