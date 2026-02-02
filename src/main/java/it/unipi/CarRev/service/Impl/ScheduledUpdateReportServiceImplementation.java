package it.unipi.CarRev.service.Impl;

import it.unipi.CarRev.config.RedisConfig;
import it.unipi.CarRev.model.Review;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.HashSet;
import java.util.Set;

@Service
public class ScheduledUpdateReportServiceImplementation{
    private final MongoTemplate mongoTemplate;
    public ScheduledUpdateReportServiceImplementation(MongoTemplate mongoTemplate){
        this.mongoTemplate=mongoTemplate;
    }
    @Scheduled(cron="0 */10 * * * *")
    public void updateReport(){
        Set<String> allKeys=getKeys();
        if(allKeys.isEmpty()){
            System.out.print("no new reports");
            return;
        }
        BulkOperations bulkOperations=mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Review.class);
        Boolean bulkHasInstruction=false;
        System.out.println(allKeys.stream().toList());
        try(Jedis jedis=RedisConfig.getJedis()){
            for(String keys:allKeys){
                String[] splitKeys=keys.split(":");
                String mongoReviewId=splitKeys[1];
                Integer redisReport=Integer.valueOf(jedis.getSet(keys,"0"));
                if(redisReport==0){
                    continue;
                }
                Query query=new Query(Criteria.where("id").is(mongoReviewId));
                Update update=new Update().inc("report",redisReport);
                bulkOperations.updateOne(query,update);
                bulkHasInstruction=true;
                System.out.println(redisReport);
            }
            if(bulkHasInstruction){
                bulkOperations.execute();
                System.out.println("migration of reports to mongoDB successful");
            }
            for(String keys:allKeys){
                if("0".equals(jedis.get(keys))){
                    jedis.del(keys);
                }
            }
        }
        catch(Exception e){
            System.err.println("error during the migration of views from redis to mongoDB:"+e.getMessage());
        }

    }
    private Set<String> getKeys(){
        Set<String> allKeys=new HashSet<>();
        String cursor= ScanParams.SCAN_POINTER_START;
        ScanParams scanParams=new ScanParams().match("Reviews:*:numReports").count(100);
        try(Jedis jedis= RedisConfig.getJedis()){
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
