package it.unipi.CarRev.controller;

import it.unipi.CarRev.service.Impl.DeleteReviewServiceImplementation;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final DeleteReviewServiceImplementation deleteReviewServiceImplementation;
    public UserController(DeleteReviewServiceImplementation deleteReviewServiceImplementation){
        this.deleteReviewServiceImplementation=deleteReviewServiceImplementation;
    }

    @GetMapping("/delReview")
    public ResponseEntity<String> deleteReview(@NotNull @RequestParam String reviewId){
        deleteReviewServiceImplementation.deleteAReview(reviewId);
        return ResponseEntity.ok("reviews correctly deleted");


    }
}
