package it.unipi.CarRev.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.redis.core.index.Indexed;

/***
 * this will be used for storing the information useful for the number of registered user and the unregistered visitor
 * analytics.
 * in these analytics i will be able to select a day, a week, a month or a year and see the number of user that registered in that
 * date and the number of unregistered visitors. We will also show the increase or decrease in percentage for the previous day, week, month, year;
 */
@Document(collection = "UserBasedAnalytics")
public class UserBasedAnalytics {
    @Id@Getter@Setter
    private String id;
    @Getter@Setter
    private Integer nOfRegisteredUsers;
    @Getter@Setter
    private Integer nOfUnregisteredUsers;
    @Getter@Setter@Indexed
    private String date;

    public UserBasedAnalytics(Integer nOfRegisteredUsers,Integer nOfUnregisteredUsers,String date){
        this.nOfRegisteredUsers=nOfRegisteredUsers;
        this.nOfUnregisteredUsers=nOfUnregisteredUsers;
        this.date=date;
    }

}
