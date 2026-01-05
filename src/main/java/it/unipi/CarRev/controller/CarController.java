package it.unipi.CarRev.controller;

import it.unipi.CarRev.dto.CarSearchResponse;
import it.unipi.CarRev.service.CarSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cars")
public class CarController {

    private final CarSearchService carSearchService;

    public CarController(CarSearchService carSearchService) {
        this.carSearchService = carSearchService;
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
}
