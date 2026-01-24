package it.unipi.CarRev.controller;

import it.unipi.CarRev.service.Impl.ProductYearRecomputeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/cars")
public class AdminCarController {

    private final ProductYearRecomputeService productYearRecomputeService;

    public AdminCarController(ProductYearRecomputeService productYearRecomputeService) {
        this.productYearRecomputeService = productYearRecomputeService;
    }

    @PostMapping("/recompute-product-year")
    public ResponseEntity<Map<String, Object>> recomputeProductYear() {
        int updated = productYearRecomputeService.recomputeAllCars();
        return ResponseEntity.ok(Map.of("updatedCars", updated));
    }
}
