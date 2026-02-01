package it.unipi.CarRev.controller;

import it.unipi.CarRev.dto.*;
import it.unipi.CarRev.service.CarSearchService;
import it.unipi.CarRev.service.Impl.LastFiveCarServiceImplementation;
import it.unipi.CarRev.service.Impl.VisitACarService;
import it.unipi.CarRev.service.Impl.WriteReviewServiceImpl;
import it.unipi.CarRev.service.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
public class CarController {

    private final CarSearchService carSearchService;
    private final VisitACarService visitACarService;
    private final LastFiveCarServiceImplementation lastFiveCarServiceImplementation;
    private final WriteReviewServiceImpl writeReviewServiceImpl;

    public CarController(CarSearchService carSearchService, VisitACarService visitACarService, LastFiveCarServiceImplementation lastFiveCarServiceImplementation, WriteReviewServiceImpl writeReviewServiceImpl) {
        this.carSearchService = carSearchService;
        this.visitACarService = visitACarService;
        this.lastFiveCarServiceImplementation=lastFiveCarServiceImplementation;
        this.writeReviewServiceImpl = writeReviewServiceImpl;
    }

    @GetMapping
    public ResponseEntity<CarSearchResponse> search(
            @RequestParam(required = false) String carName,
            @RequestParam(required = false) String carBrand,
            @RequestParam(required = false) String carModel,
            @RequestParam(required = false) String bodyType,
            @RequestParam(required = false) Integer engineDisplacement,
            @RequestParam(required = false) Integer numberOfCylinders,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                carSearchService.search(
                        carName,
                        carBrand,
                        carModel,
                        bodyType,
                        engineDisplacement,
                        numberOfCylinders,
                        page,
                        size
                )
        );
    }
    @GetMapping("/visitCar")
    public ResponseEntity<FullCarInfoDTO> visitACar(@RequestParam(required = true) String id){
        FullCarInfoDTO fullCarInfoDTO=visitACarService.getCarById(id);
        if(fullCarInfoDTO==null){
           return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(fullCarInfoDTO);
    }
    //remember to lock this in the security config
    @GetMapping("/logged/lastFive")
    public ResponseEntity<List<FrontPageCarSummaryDTO>> lastFiveCar(){
        List<FrontPageCarSummaryDTO> lastCars=lastFiveCarServiceImplementation.getLastFiveCar();
        return ResponseEntity.ok(lastCars);

    }
    @PostMapping("/logged/review")
    public ResponseEntity<String>reviewCar(@Valid @RequestBody(required = true)InsertReviewRequestDTO request){
        try{
            Boolean results=writeReviewServiceImpl.writeReview(request);
            return ResponseEntity.ok("Review correctly inserted");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
        /*if(results){
            return ResponseEntity.ok("Review correctly inserted");
        }
        return ResponseEntity.notFound().build();*/
    }
}
