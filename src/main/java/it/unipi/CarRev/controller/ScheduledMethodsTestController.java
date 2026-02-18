package it.unipi.CarRev.controller;

import it.unipi.CarRev.service.Impl.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/scheduled")
public class ScheduledMethodsTestController {
    private final ScheduledUpdateLikesImplementation scheduledUpdateLikesImplementation;
    private final ScheduledRemoveReviewsJobServiceImplementation scheduledRemoveReviewsJobServiceImplementation;
    private final ScheduledUpdateReportServiceImplementation scheduledUpdateReportServiceImplementation;
    private final ScheduledUpdateViewsService scheduledUpdateViewsService;
    private final TrafficForInfoServiceImpl trafficForInfoService;
    private final ScheduledCarAnalyticsService scheduledCarAnalyticsService;
    public ScheduledMethodsTestController(ScheduledUpdateLikesImplementation scheduledUpdateLikesImplementation, ScheduledRemoveReviewsJobServiceImplementation scheduledRemoveReviewsJobServiceImplementation, ScheduledUpdateReportServiceImplementation scheduledUpdateReportServiceImplementation, ScheduledUpdateViewsService scheduledUpdateViewsService, TrafficForInfoServiceImpl trafficForInfoService, ScheduledCarAnalyticsService scheduledCarAnalyticsService) {
        this.scheduledUpdateLikesImplementation = scheduledUpdateLikesImplementation;
        this.scheduledRemoveReviewsJobServiceImplementation = scheduledRemoveReviewsJobServiceImplementation;
        this.scheduledUpdateReportServiceImplementation = scheduledUpdateReportServiceImplementation;
        this.scheduledUpdateViewsService = scheduledUpdateViewsService;
        this.trafficForInfoService = trafficForInfoService;
        this.scheduledCarAnalyticsService = scheduledCarAnalyticsService;
    }

    @PostMapping("/likes")
    public ResponseEntity<String> updateLikes() {
        try {
            scheduledUpdateLikesImplementation.scheduledUpdateLikes();
            return ResponseEntity.ok("migration of likes successful");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("error during likes migration" + e.getMessage());
        }
    }
    @PostMapping("/orphan")
    public ResponseEntity<String> deleteOrphan(){
        try{
            scheduledRemoveReviewsJobServiceImplementation.deleteOrphanReview();
            return ResponseEntity.ok("orphan deleted successfully");
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body("error during orphan delete" + e.getMessage());
        }
    }
    @PostMapping("/reports")
    public ResponseEntity<String> updateReports(){
        try{
            scheduledUpdateReportServiceImplementation.updateReport();
            return ResponseEntity.ok("report updated successfully");
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body("error during report update" + e.getMessage());
        }
    }
    @PostMapping("/views")
    public ResponseEntity<String> updateViews(){
        try{
            scheduledUpdateViewsService.updateMongoViews();
            return ResponseEntity.ok("views updated successfully");
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body("error during the update of views" + e.getMessage());
        }
    }
    @PostMapping("/trafficInfo")
    public ResponseEntity<String> insertTrafficInfo(){
        try{
            trafficForInfoService.dailyTrafficInfoTransfer();
            return ResponseEntity.ok("traffic info updated successfully");
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body("error during the insert of the traffic info" + e.getMessage());
        }
    }
    @PostMapping("/carInfo")
    public ResponseEntity<String> insertCarInfo(){
        try{
            scheduledCarAnalyticsService.dailyCarAnalyticsSnapshot();
            return ResponseEntity.ok("Car analytics uploaded successfully");
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body("error during the upload of car analytics" + e.getMessage());
        }
    }

}
