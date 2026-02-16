package it.unipi.CarRev.service.Impl;


import com.mongodb.bulk.BulkWriteResult;
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
                Integer numOfLikes=Integer.valueOf(jedis.getSet(key,"0"));
                if(numOfLikes==0){
                    continue;
                }
                System.out.println("numberOfLikes: "+numOfLikes);
                /*
                Query reviewQuery=new Query(Criteria.where("id").is(reviewId));
                Update reviewUpdate=new Update().inc("likes",numOfLikes);
                 */
                /*
                Query userQuery=new Query(new Criteria().orOperator(
                        Criteria.where("reviews._id").is(reviewIdObj),
                        Criteria.where("otherReviews._id").is(reviewIdObj)
                ));
                Update userUpdate=new Update()
                        .filterArray(Criteria.where("rev._id").is(reviewIdObj))
                        .inc("reviews.$[rev].likes",numOfLikes)
                        .filterArray(Criteria.where("otherRev._id").is(reviewIdObj))
                        .inc("otherReviews.$[otherRev].likes",numOfLikes);


                Query carQuery=new Query(new Criteria().orOperator(
                        Criteria.where("Top_Ten_Review._id").is(reviewIdObj),
                        Criteria.where("Other_review._id").is(reviewIdObj)
                ));
                Update carUpdate=new Update()
                        .filterArray(Criteria.where("rev._id").is(reviewIdObj))
                        .inc("Top_Ten_Review.$[rev].likes",numOfLikes)
                        .filterArray(Criteria.where("otherRev._id").is(reviewIdObj))
                        .inc("Other_review.$[otherRev].likes",numOfLikes);


               // reviewOps.updateOne(reviewQuery,reviewUpdate);
                userOps.updateOne(userQuery,userUpdate);
                carOps.updateOne(carQuery,carUpdate);

                 */

                userOps.updateOne(
                        new Query(Criteria.where("reviews._id").is(reviewIdObj)),
                        new Update().inc("reviews.$.likes", numOfLikes)
                );
                userOps.updateOne(
                        new Query(Criteria.where("otherReviews._id").is(reviewIdObj)),
                        new Update().inc("otherReviews.$.likes", numOfLikes)
                );
                carOps.updateOne(
                        new Query(Criteria.where("Top_Ten_Review._id").is(reviewIdObj)),
                        new Update().inc("Top_Ten_Review.$.likes", numOfLikes)
                );
                carOps.updateOne(
                        new Query(Criteria.where("Other_review._id").is(reviewIdObj)),
                        new Update().inc("Other_review.$.likes", numOfLikes)
                );
                hasOps=true;
            }
            if(hasOps){
               // reviewOps.execute();
                BulkWriteResult userResult=userOps.execute();
                BulkWriteResult carResult = carOps.execute();
                System.out.println("USER: Matchati=" + userResult.getMatchedCount() + ", Modificati=" + userResult.getModifiedCount());
                System.out.println("CAR: Matchati=" + carResult.getMatchedCount() + ", Modificati=" + carResult.getModifiedCount());
                System.out.println("migration of likes from redis to mongoDB has been successful");
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
