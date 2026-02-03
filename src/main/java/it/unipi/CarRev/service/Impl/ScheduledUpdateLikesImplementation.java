package it.unipi.CarRev.service.Impl;


import it.unipi.CarRev.config.RedisConfig;
import it.unipi.CarRev.model.Car;
import it.unipi.CarRev.model.Review;
import it.unipi.CarRev.model.User;
import org.bson.types.ObjectId;
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
public class ScheduledUpdateLikesImplementation {
    private final MongoTemplate mongoTemplate;
    public ScheduledUpdateLikesImplementation(MongoTemplate mongoTemplate){
        this.mongoTemplate=mongoTemplate;
    }

    @Scheduled(cron="0 */10 * * * *")
    public void scheduledUpdateLikes(){
        Set<String> allKeys=getKeys();
        if(allKeys.isEmpty()){
            System.out.println("no new likes inserted");
            return;
        }
        BulkOperations reviewOps=mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Review.class);
        BulkOperations userOps=mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, User.class);
        BulkOperations carOps=mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Car.class);
        Boolean hasOps=false;
        try(Jedis jedis=RedisConfig.getJedis()) {
            for (String key : allKeys) {
                String reviewId = key.split(":")[1];
                ObjectId reviewIdObj= new ObjectId(reviewId);
                Integer numOfLikes=Integer.valueOf(jedis.setGet(key,"0"));
                if(numOfLikes==0){
                    continue;
                }
                Query reviewQuery=new Query(Criteria.where("id").is(reviewId));
                Update reviewUpdate=new Update().inc("likes",numOfLikes);

                Query userQuery=new Query(Criteria.where("reviews._id").is(reviewIdObj));
                Update userUpdate=new Update().inc("reviews.$.likes",numOfLikes);

                Query carQuery=new Query(Criteria.where("Top_Ten_Review._id").is(reviewIdObj));
                Update carUpdate=new Update().inc("Top_Ten_Review.$.likes",numOfLikes);

                reviewOps.updateOne(reviewQuery,reviewUpdate);
                userOps.updateOne(userQuery,userUpdate);
                carOps.updateOne(carQuery,carUpdate);
                hasOps=true;
            }
            if(hasOps){
                reviewOps.execute();
                userOps.execute();
                carOps.execute();
                System.out.println("migration of likes from redis to mongoDB has been successfull");
            }
            for(String key: allKeys){
                if("0".equals(jedis.get(key))){
                    jedis.del(key);
                }
            }
        }
        catch (Exception e){
            System.err.println("Error during the migration of likes from redis to mongoDB:"+e.getMessage());
        }

    }
    private Set<String> getKeys(){
        Set<String> allKeys=new HashSet<>();
        String cursor= ScanParams.SCAN_POINTER_START;
        ScanParams scanParams=new ScanParams().match("Reviews:*:numLikes").count(100);
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
