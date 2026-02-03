package it.unipi.CarRev.service.Impl;

import io.lettuce.core.api.sync.RedisAclCommands;
import it.unipi.CarRev.config.RedisConfig;
import it.unipi.CarRev.model.Car;
import it.unipi.CarRev.utils.RedisUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
public class ScheduledUpdateGeneralScoreServiceImplementation {
    private final MongoTemplate mongoTemplate;
    public ScheduledUpdateGeneralScoreServiceImplementation(MongoTemplate mongoTemplate){
        this.mongoTemplate=mongoTemplate;
    }
    @Scheduled(cron="0 */10 * * * *")
    public void scheduledScoreUpdate(){
        Set<String> allKeys=RedisUtils.getSring("Cars:*:numberOfReviews");
        if(allKeys.isEmpty()){
            System.out.println("no new reviews");
            return;
        }
        BulkOperations carBulk=mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Car.class);
        Boolean hasOp=false;
        try(Jedis jedis=RedisConfig.getJedis()){
            for (String key : allKeys) {
                String carId = key.split(":")[1];
                String totalScoreKey = "Cars:" + carId + ":totalRating";
                Integer nOfReviews=Integer.valueOf(jedis.setGet(key,"0"));
                Double totalScore=Double.valueOf(jedis.setGet(totalScoreKey,"0.0"));
                if(nOfReviews==0){
                    continue;
                }
                List<Document> pipeline= Arrays.asList(
                        new Document("$set",new Document()
                                .append("total_review_score",new Document("$add",Arrays.asList("$total_review_score",totalScore)))
                                .append("number_of_reviews",new Document("$add",Arrays.asList("$number_of_reviews",nOfReviews)))
                        ),
                        new Document("$set",new Document("general_rating",
                                new Document("$divide",Arrays.asList("$total_review_score","$number_of_reviews"))))
                );
                Query query=new Query(Criteria.where("_id").is(new ObjectId(carId)));
                Update update=Update.fromDocument(new Document("$set",pipeline));
                carBulk.updateOne(query,update);
                hasOp=true;
            }
            if(hasOp){
                carBulk.execute();
                System.out.println("scheduled update for general rating completed successfully");
            }
            for(String key: allKeys){
                String carId=key.split(":")[1];
                String totalScoreKey = "Cars:" + carId + ":totalRating";
                if("0".equals(jedis.get(key))){
                    jedis.del(key);
                }
                if("0.0".equals(jedis.get(totalScoreKey)) || "0".equals(jedis.get(totalScoreKey))){
                    jedis.del(totalScoreKey);
                }

            }
        }
        catch(Exception e){
            System.out.println("error during scheduled update of the general score:"+e.getMessage());
        }
    }
}
