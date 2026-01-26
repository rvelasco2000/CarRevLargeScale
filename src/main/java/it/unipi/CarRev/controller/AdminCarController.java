package it.unipi.CarRev.controller;

import it.unipi.CarRev.dto.CarCreateRequestDTO;
import it.unipi.CarRev.service.Impl.InsertNewCarServiceImpl;
import it.unipi.CarRev.service.Impl.ProductYearRecomputeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/cars")
public class AdminCarController {

    private final ProductYearRecomputeService productYearRecomputeService;
    private final InsertNewCarServiceImpl insertNewCarService;
    public AdminCarController(ProductYearRecomputeService productYearRecomputeService, InsertNewCarServiceImpl insertNewCarService){
        this.productYearRecomputeService = productYearRecomputeService;
        this.insertNewCarService=insertNewCarService;
    }

    @PostMapping("/insert")
    public ResponseEntity<String> insertCar(@Valid @RequestBody CarCreateRequestDTO request){
        Boolean result=insertNewCarService.insertCar(request);
        if(result){
            return ResponseEntity.ok("car has been inserted successfully");
        }
        return ResponseEntity.internalServerError().body("an error has occurred in the insertion of a new car");
    }
    @PostMapping("/recompute-product-year")
    public ResponseEntity<Map<String, Object>> recomputeProductYear() {
        int updated = productYearRecomputeService.recomputeAllCars();
        return ResponseEntity.ok(Map.of("updatedCars", updated));
    }
}
