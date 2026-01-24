package it.unipi.CarRev.service.Impl;

import it.unipi.CarRev.Enum.DateEnum;
import it.unipi.CarRev.dto.TrafficInfoAnalyticsResultDTO;
import it.unipi.CarRev.dto.UserBasedAnalyticsResponseDTO;
import it.unipi.CarRev.model.UserBasedAnalytics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.w3c.dom.stylesheets.LinkStyle;

import java.time.LocalDate;
import java.util.List;

@Service
public class UserBasedAnalyticsYMWDServiceImplementation {

    private final MongoTemplate mongoTemplate;

    public UserBasedAnalyticsYMWDServiceImplementation(MongoTemplate mongoTemplate){
        this.mongoTemplate=mongoTemplate;
    }

    public UserBasedAnalyticsResponseDTO getUsersAnalytics(String date, DateEnum period){

        LocalDate targetDate=LocalDate.parse(date);
        LocalDate startDate=LocalDate.ofEpochDay(0);
        LocalDate endDate=LocalDate.ofEpochDay(0);
        switch(period){
            case YEARLY -> {
                startDate=targetDate.minusYears(1).withDayOfYear(1);
                endDate=targetDate.withDayOfYear(targetDate.lengthOfYear());
            }
            case MONTHLY -> {
                startDate=targetDate.minusMonths(1).withDayOfMonth(1);
                endDate=targetDate.withDayOfMonth(targetDate.lengthOfMonth());
            }
            case WEEKLY -> {
                startDate=targetDate.minusWeeks(1).minusDays(targetDate.getDayOfWeek().getValue()-1);
                endDate=targetDate.plusDays(7-targetDate.getDayOfWeek().getValue());
            }
            case DAILY -> {
                startDate=targetDate.minusDays(1);
                endDate=targetDate;
            }
        }
        MatchOperation filter= Aggregation.match(
                Criteria.where("date").gte(startDate.toString()).lte(endDate.toString())
        );
        ProjectionOperation project=Aggregation.project("nOfRegisteredUsers","nOfUnregisteredUsers")
                .and(DateOperators.DateToString.dateOf(
                        DateOperators.DateFromString.fromStringOf("date")
                ).toString(period.format)).as("periodGroup");
        GroupOperation group=Aggregation.group("periodGroup")
                .sum("nOfRegisteredUsers").as("totalRegisteredUsers")
                .sum("nOfUnregisteredUsers").as("nOfUnregisteredUsers")
                .first("periodGroup").as("period");
        SortOperation sort=Aggregation.sort(org.springframework.data.domain.Sort.Direction.ASC, "period");
        Aggregation aggregation=Aggregation.newAggregation(
                filter,
                project,
                group,
                sort
        );
        List<UserBasedAnalyticsResponseDTO> results=mongoTemplate.aggregate(aggregation,"UserBasedAnalytics", UserBasedAnalyticsResponseDTO.class).getMappedResults();
        System.out.println("DEBUG: documents for traffic info analytics:"+results);
        getTrends(results);
        return results.get(results.size()-1);
    }

    /***
     * this methods will recive 2 results and i need to calculate the trend between them
     * @param results
     */
    private void getTrends(List<UserBasedAnalyticsResponseDTO> results){
        for(int i=1;i<results.size();i++){
            UserBasedAnalyticsResponseDTO current=results.get(i);
            UserBasedAnalyticsResponseDTO last=results.get(i-1);

            current.setRegisteredUsersTrend(getPerc(current.getTotalRegisteredUsers(), last.getTotalRegisteredUsers()));
            current.setUnregisteredVisitorsTrend(getPerc(current.getTotalUnregisteredVisitors(),last.getTotalUnregisteredVisitors()));
        }
    }
    private double getPerc(int current,int prev){
        if(prev==0){
            if(current>0){
                return 100;
            }
            else{
                return 0;
            }
        }
        return (double) (current - prev) /prev*100;
    }

}
