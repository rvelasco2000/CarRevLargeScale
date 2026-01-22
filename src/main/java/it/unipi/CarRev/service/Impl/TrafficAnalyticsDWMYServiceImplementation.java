package it.unipi.CarRev.service.Impl;


import it.unipi.CarRev.Enum.DateEnum;
import it.unipi.CarRev.dao.mongo.TrafficForAnalyticsDAO;
import it.unipi.CarRev.dto.TrafficInfoAnalyticsResultDTO;
import org.springframework.data.mongodb.core.MongoActionOperation;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;

/***
 * this class will allow us to implement the logic for the analytics of the traffic information about
 * the number of legitimate user and bot user the user will select the starting date and the end date and the
 * results will be shown in daily,weekly,monthly,yearly fashion.
 */
@Service
public class TrafficAnalyticsDWMYServiceImplementation {
    private final MongoTemplate mongoTemplate;
    public TrafficAnalyticsDWMYServiceImplementation(MongoTemplate mongoTemplate){
        this.mongoTemplate=mongoTemplate;
    }

    public List<TrafficInfoAnalyticsResultDTO> getTrafficInfoAnalytics(String startDate, String endDate, DateEnum period){
        MatchOperation filter= Aggregation.match(
                Criteria.where("date").gte(startDate).lte(endDate)
        );

        ProjectionOperation project = Aggregation.project("nOfLegitimateUsers", "nOfSuspiciousUsers")
                .and(DateOperators.DateToString.dateOf(
                        DateOperators.DateFromString.fromStringOf("date")
                ).toString(period.format)).as("periodGroup");
        GroupOperation group=Aggregation.group("periodGroup")
                .sum("nOfLegitimateUsers").as("totalLegitimateUsersForPeriod")
                .sum("nOfSuspiciousUsers").as("totalSuspiciousUsersForPeriod")
                .first("periodGroup").as("period");
        SortOperation sort=Aggregation.sort(org.springframework.data.domain.Sort.Direction.ASC, "period");
        Aggregation aggregation=Aggregation.newAggregation(
                filter,
                project,
                group,
                sort
        );
        System.out.println("DEBUG: documents for traffic info analytics:"+mongoTemplate.aggregate(aggregation,"TrafficForAnalytics",TrafficInfoAnalyticsResultDTO .class).getMappedResults());
        return mongoTemplate.aggregate(aggregation,"TrafficForAnalytics",TrafficInfoAnalyticsResultDTO .class).getMappedResults();
    }

}
