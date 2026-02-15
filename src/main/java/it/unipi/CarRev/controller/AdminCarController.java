package it.unipi.CarRev.controller;

import it.unipi.CarRev.dto.CarCreateRequestDTO;
import it.unipi.CarRev.dto.CarUpdateRequestDTO;
import it.unipi.CarRev.dto.ReportedReviewResponseDTO;
import it.unipi.CarRev.service.Impl.*;
import it.unipi.CarRev.service.exception.ForbiddenException;
import it.unipi.CarRev.service.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/cars")
public class AdminCarController {

    private final ProductYearRecomputeService productYearRecomputeService;
    private final InsertNewCarServiceImpl insertNewCarService;
    private final UpdateCarServiceImpl updateCarService;
    private final DeleteACarServiceImpl deleteACarService;
    private final GetReportedReviewsServiceImplementation getReportedReviewsServiceImplementation;
    private final ClearAReviewReportServiceImplementation clearAReviewReportServiceImplementation;
    private final DeleteReviewServiceImplementation deleteReviewServiceImplementation;
    public AdminCarController(ProductYearRecomputeService productYearRecomputeService, InsertNewCarServiceImpl insertNewCarService, UpdateCarServiceImpl updateCarService,DeleteACarServiceImpl deleteACarService, GetReportedReviewsServiceImplementation getReportedReviewsServiceImplementation, ClearAReviewReportServiceImplementation clearAReviewReportServiceImplementation,DeleteReviewServiceImplementation deleteReviewServiceImplementation){
        this.productYearRecomputeService = productYearRecomputeService;
        this.insertNewCarService=insertNewCarService;
        this.updateCarService=updateCarService;
        this.deleteACarService=deleteACarService;
        this.getReportedReviewsServiceImplementation=getReportedReviewsServiceImplementation;
        this.clearAReviewReportServiceImplementation=clearAReviewReportServiceImplementation;
        this.deleteReviewServiceImplementation=deleteReviewServiceImplementation;
    }
    @GetMapping("/deleteReview")
    public ResponseEntity<String> deleteReview(@NotNull @RequestParam String reviewId){
        try {
            deleteReviewServiceImplementation.deleteAReview(reviewId);
            return ResponseEntity.ok("reviews correctly deleted");
        }
        catch(ResourceNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/insert")
    public ResponseEntity<String> insertCar(@Valid @RequestBody CarCreateRequestDTO request){
        Boolean result=insertNewCarService.insertCar(request);
        if(result){
            return ResponseEntity.ok("car has been inserted successfully");
        }
        return ResponseEntity.internalServerError().body("an error has occurred in the insertion of a new car");
    }

    /***
     * we need to discuss the consistency between the various features i belive we can leave it in the last 5 car but
     * we need to figure it out something for neo4j
     * @param request
     * @return
     */
    @PostMapping("/update")
    public ResponseEntity<String> updateCar(@Valid @RequestBody CarUpdateRequestDTO request){
        int result=updateCarService.updateCar(request);
        switch(result){
            case -1:
                return ResponseEntity.notFound().build();
            case -2:
                return ResponseEntity.internalServerError().body("an error has occurred during the update of a car");
            case 0:
                return ResponseEntity.ok("car has been successfully updated");
        }
        return ResponseEntity.internalServerError().body("an error has occurred during the update of a car");
    }

    /***
     * TODO when deleting a car i must delete also in the last 5 visited, to do so when i visit a car if that car
     * does not exists i check in the user last 5 and delete it
     * we will also need to address neo4j consistency
     * @param id
     * @return
     */
    @PostMapping("/delete")
    public ResponseEntity<String> deleteCar(@RequestParam(required = true) String id){
        try{
            deleteACarService.deleteCar(id);
            return ResponseEntity.ok("car successfully deleted");
        }
        catch(ResourceNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch(RuntimeException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
        //int results=deleteACarService.deleteCar(id);
        /*
        switch(results){
            case -1:
                return ResponseEntity.notFound().build();
            case -2:
                return ResponseEntity.internalServerError().body("an error has occurred during the delete of a car");
            case 0:
                return ResponseEntity.ok("car has been successfully deleted");
        }
        return ResponseEntity.internalServerError().body("an error has occurred during the delete of a car");
        */

    }
    @PostMapping("/recompute-product-year")
    public ResponseEntity<Map<String, Object>> recomputeProductYear() {
        int updated = productYearRecomputeService.recomputeAllCars();
        return ResponseEntity.ok(Map.of("updatedCars", updated));
    }
    @GetMapping("/showReportedReviews")
    public ResponseEntity<Page<ReportedReviewResponseDTO>> getReportedReviews(Integer numPage){


        Page<ReportedReviewResponseDTO> result=getReportedReviewsServiceImplementation.getReportedReviews(numPage);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/resetReport")
    public ResponseEntity<String> resetReport(String reviewId){
        try{
            clearAReviewReportServiceImplementation.clearAReview(reviewId);
            return ResponseEntity.ok("report in reviews has been reset");
        }
        catch(ForbiddenException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error during the reset of reports");
        }
    }
}
