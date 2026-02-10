package it.unipi.CarRev.controller;

import it.unipi.CarRev.dto.CarAnalyticsBetweenDatesRequestDTO;
import it.unipi.CarRev.dto.CarAnalyticsBetweenDatesResponseDTO;
import it.unipi.CarRev.dto.TrafficInfoAnalyticsRequestDTO;
import it.unipi.CarRev.dto.TrafficInfoAnalyticsResultDTO;
import it.unipi.CarRev.dto.UserBasedAnalyticsRequestDTO;
import it.unipi.CarRev.dto.UserBasedAnalyticsResponseDTO;
import it.unipi.CarRev.service.Impl.CarAnalyticsBetweenDatesService;
import it.unipi.CarRev.service.Impl.TrafficAnalyticsDWMYServiceImplementation;
import it.unipi.CarRev.service.Impl.UserBasedAnalyticsYMWDServiceImplementation;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
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
    private CarAnalyticsBetweenDatesService carAnalyticsBetweenDatesService;
    @Autowired
    public AnalyticsController(TrafficAnalyticsDWMYServiceImplementation trafficAnalyticsDWMYServiceImplementation,
                               UserBasedAnalyticsYMWDServiceImplementation userBasedAnalyticsYMWDServiceImplementation,
                               CarAnalyticsBetweenDatesService carAnalyticsBetweenDatesService){
        this.userBasedAnalyticsYMWDServiceImplementation=userBasedAnalyticsYMWDServiceImplementation;
        this.trafficAnalyticsDWMYServiceImplementation=trafficAnalyticsDWMYServiceImplementation;
        this.carAnalyticsBetweenDatesService = carAnalyticsBetweenDatesService;
    }
    @PostMapping("/userAnalytics")
    public ResponseEntity<?> getUserAnalytics(@Valid@RequestBody UserBasedAnalyticsRequestDTO requestDTO){
        LocalDate date=LocalDate.parse(requestDTO.getDate());
        if(date.isAfter(LocalDate.now())){
            return ResponseEntity.badRequest().body("ERROR:the date in the request is in the future");
        }
        UserBasedAnalyticsResponseDTO responseDTO=userBasedAnalyticsYMWDServiceImplementation.getUsersAnalytics(
          requestDTO.getDate(),
          requestDTO.getPeriod()
        );
        return ResponseEntity.ok(responseDTO);
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

    @PostMapping("/carTopBetweenDates")
    public ResponseEntity<?> getTopCarsBetweenDates(@Valid @RequestBody CarAnalyticsBetweenDatesRequestDTO requestDTO){
        LocalDate start=LocalDate.parse(requestDTO.getStartDate());
        LocalDate end=LocalDate.parse(requestDTO.getEndDate());
        if(start.isAfter(end)){
            return ResponseEntity.badRequest().body("ERROR:The end date is before the start date");
        }
        CarAnalyticsBetweenDatesResponseDTO response =
                carAnalyticsBetweenDatesService.getTopCarsBetween(requestDTO.getStartDate(), requestDTO.getEndDate());
        if (response == null) {
            return ResponseEntity.badRequest().body("ERROR:missing analytics data for one or both dates");
        }
        return ResponseEntity.ok(response);
    }
}
