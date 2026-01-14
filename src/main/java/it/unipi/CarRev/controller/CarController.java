package it.unipi.CarRev.controller;

import it.unipi.CarRev.dto.CarSearchResponse;
import it.unipi.CarRev.dto.CarSummaryDTO;
import it.unipi.CarRev.dto.FullCarInfoDTO;
import it.unipi.CarRev.service.CarSearchService;
import it.unipi.CarRev.service.Impl.LastFiveCarServiceImplementation;
import it.unipi.CarRev.service.Impl.VisitACarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.util.RecyclerPool;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
public class CarController {

    private final CarSearchService carSearchService;
    private final VisitACarService visitACarService;
    private LastFiveCarServiceImplementation lastFiveCarServiceImplementation;

    public CarController(CarSearchService carSearchService, VisitACarService visitACarService) {
        this.carSearchService = carSearchService;
        this.visitACarService = visitACarService;
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
            ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(fullCarInfoDTO);
    }
    //remember to lock this in the security config
    @GetMapping("/logged/lastFive")
    public ResponseEntity<List<CarSummaryDTO>> lastFiveCar(){
        List<CarSummaryDTO> lastCars=lastFiveCarServiceImplementation.getLastFiveCar();
        return ResponseEntity.ok(lastCars);

    }
}
