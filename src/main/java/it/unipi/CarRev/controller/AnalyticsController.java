package it.unipi.CarRev.controller;

import it.unipi.CarRev.dto.TrafficInfoAnalyticsRequestDTO;
import it.unipi.CarRev.dto.TrafficInfoAnalyticsResultDTO;
import it.unipi.CarRev.service.Impl.TrafficAnalyticsDWMYServiceImplementation;
import it.unipi.CarRev.service.Impl.UserBasedAnalyticsYMWDServiceImplementation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
public class AnalyticsController {
    private TrafficAnalyticsDWMYServiceImplementation trafficAnalyticsDWMYServiceImplementation;
    private UserBasedAnalyticsYMWDServiceImplementation userBasedAnalyticsYMWDServiceImplementation;
    @Autowired
    public AnalyticsController(TrafficAnalyticsDWMYServiceImplementation trafficAnalyticsDWMYServiceImplementation,UserBasedAnalyticsYMWDServiceImplementation userBasedAnalyticsYMWDServiceImplementation){
        this.userBasedAnalyticsYMWDServiceImplementation=userBasedAnalyticsYMWDServiceImplementation;
        this.trafficAnalyticsDWMYServiceImplementation=trafficAnalyticsDWMYServiceImplementation;
    }


    @PostMapping("/trafficInfo")
    public ResponseEntity<?> getAnalytics(@Valid @RequestBody TrafficInfoAnalyticsRequestDTO requestDTO){ //the date must follow the YYYY-MM-dd format
        LocalDate start=LocalDate.parse(requestDTO.getStartDate());
        LocalDate end=LocalDate.parse(requestDTO.getEndDate());
        if(start.isAfter(end)){
           return ResponseEntity.badRequest().body("ERROR:The end date is before the start date");
        }
        List<TrafficInfoAnalyticsResultDTO> result=trafficAnalyticsDWMYServiceImplementation.getTrafficInfoAnalytics(
            requestDTO.getStartDate(),
            requestDTO.getEndDate(),
            requestDTO.getPeriod()
        );
        return ResponseEntity.ok(result);
    }
}
