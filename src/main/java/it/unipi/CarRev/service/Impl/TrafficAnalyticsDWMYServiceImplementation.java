package it.unipi.CarRev.service.Impl;


import it.unipi.CarRev.Enum.DateEnum;
import it.unipi.CarRev.dao.mongo.TrafficForAnalyticsDAO;
import it.unipi.CarRev.dto.TrafficInfoAnalyticsResultDTO;
import org.springframework.data.mongodb.core.MongoActionOperation;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
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
        LocalDate targetStart=LocalDate.parse(startDate);
        LocalDate targetEnd=LocalDate.parse(endDate);
        LocalDate newStartDate=LocalDate.ofEpochDay(0);
        LocalDate newEndDate=LocalDate.ofEpochDay(0);
        switch (period){
            case YEARLY -> {
                newStartDate=targetStart.withDayOfYear(1);
                newEndDate=targetEnd.withDayOfYear(targetEnd.lengthOfYear());
            }
            case MONTHLY-> {
                newStartDate=targetStart.withDayOfMonth(1);
                newEndDate=targetEnd.withDayOfMonth(targetEnd.lengthOfMonth());
            }
            case WEEKLY -> {
                newStartDate=targetStart.with(DayOfWeek.MONDAY);
                newEndDate=targetEnd.with(DayOfWeek.SUNDAY);
            }
            case DAILY -> {
                newStartDate=targetStart;
                newEndDate=targetEnd;
            }
        }
        System.out.println("DEBUG: Traffic analytics start date:"+newStartDate+" end date:"+newEndDate);

        MatchOperation filter= Aggregation.match(
                Criteria.where("date").gte(newStartDate.toString()).lte(newEndDate.toString())
        );

        ProjectionOperation project = Aggregation.project("nOfLegitimateUsers", "nOfSuspiciousUsers")
                .and(DateOperators.DateToString.dateOf(
                        DateOperators.DateFromString.fromStringOf("date")
                ).toString(period.format)).as("periodGroup");
        GroupOperation group=Aggregation.group("periodGroup")
                .sum("nOfLegitimateUsers").as("totalLegitimateUsersForPeriod")
                .sum("nOfSuspiciousUsers").as("totalSuspiciousUsersForPeriod")
                .first("periodGroup").as("period");
        ProjectionOperation getRatio=Aggregation.project("period","totalLegitimateUsersForPeriod","totalSuspiciousUsersForPeriod")
                .and(
                        ArithmeticOperators.Multiply.valueOf(
                                ArithmeticOperators.Divide.valueOf("totalSuspiciousUsersForPeriod")
                                        .divideBy(ConditionalOperators.when(Criteria.where("totalLegitimateUsersForPeriod").is(0))
                                                .then(1)
                                                .otherwise("$totalLegitimateUsersForPeriod")
                                        )
                        ).multiplyBy(100)
                ).as("ratio");
        SortOperation sort=Aggregation.sort(org.springframework.data.domain.Sort.Direction.ASC, "period");
        Aggregation aggregation=Aggregation.newAggregation(
                filter,
                project,
                group,
                getRatio,
                sort
        );
        System.out.println("DEBUG: documents for traffic info analytics:"+mongoTemplate.aggregate(aggregation,"TrafficForAnalytics",TrafficInfoAnalyticsResultDTO .class).getMappedResults());
        return mongoTemplate.aggregate(aggregation,"TrafficForAnalytics",TrafficInfoAnalyticsResultDTO .class).getMappedResults();
    }

}
