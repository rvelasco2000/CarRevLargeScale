package it.unipi.CarRev.controller;

import it.unipi.CarRev.dto.CarCreateRequestDTO;
import it.unipi.CarRev.dto.CarUpdateRequestDTO;
import it.unipi.CarRev.service.Impl.InsertNewCarServiceImpl;
import it.unipi.CarRev.service.Impl.ProductYearRecomputeService;
import it.unipi.CarRev.service.Impl.UpdateCarServiceImpl;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
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
    private final UpdateCarServiceImpl updateCarService;
    public AdminCarController(ProductYearRecomputeService productYearRecomputeService, InsertNewCarServiceImpl insertNewCarService, UpdateCarServiceImpl updateCarService){
        this.productYearRecomputeService = productYearRecomputeService;
        this.insertNewCarService=insertNewCarService;
        this.updateCarService=updateCarService;
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
    @PostMapping("/recompute-product-year")
    public ResponseEntity<Map<String, Object>> recomputeProductYear() {
        int updated = productYearRecomputeService.recomputeAllCars();
        return ResponseEntity.ok(Map.of("updatedCars", updated));
    }
}
