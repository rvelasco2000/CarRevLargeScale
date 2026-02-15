package it.unipi.CarRev.service.Impl;


import it.unipi.CarRev.model.Review;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledRemoveReviewsJobServiceImplementation {
    private final MongoTemplate mongoTemplate;
    public ScheduledRemoveReviewsJobServiceImplementation(MongoTemplate mongoTemplate){
        this.mongoTemplate=mongoTemplate;
    }

    @Scheduled(cron="0 0 3 * * WED")
    public void deleteOrphanReview(){
        final String DELETEDUSER="Deleted User";
        final String DELETEDCAR="Deleted Car";
        Query query=new Query(Criteria.where("username").is(DELETEDUSER)
                .and("car_name").is(DELETEDCAR));
        try{
           Long result=mongoTemplate.remove(query, Review.class).getDeletedCount();
            if(result>0){
                System.out.println("Deleted "+result+" orphan reviews");
            }
            else{
                System.out.println("No orphan review deleted");
            }
        }
        catch (Exception e){
            System.out.println("Error during weekly delete of reviews");
        }
    }
}
