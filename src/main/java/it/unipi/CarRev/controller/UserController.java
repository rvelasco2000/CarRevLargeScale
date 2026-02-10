package it.unipi.CarRev.controller;

import it.unipi.CarRev.dto.ReviewUpdateRequestDTO;
import it.unipi.CarRev.service.Impl.DeleteReviewServiceImplementation;
import it.unipi.CarRev.service.Impl.UpdateReviewServiceImpl;
import it.unipi.CarRev.service.exception.ForbiddenException;
import it.unipi.CarRev.service.exception.ResourceNotFoundException;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final DeleteReviewServiceImplementation deleteReviewServiceImplementation;
    private final UpdateReviewServiceImpl updateReviewService;
    public UserController(DeleteReviewServiceImplementation deleteReviewServiceImplementation,UpdateReviewServiceImpl updateReviewService){
        this.deleteReviewServiceImplementation=deleteReviewServiceImplementation;
        this.updateReviewService=updateReviewService;
    }
    @PostMapping("/updateReview")
    public ResponseEntity<String> updateReview(@Valid @RequestBody ReviewUpdateRequestDTO request){
        try{
            updateReviewService.updateReview(request);
            return ResponseEntity.ok("review correctly updated");
        }
        catch(ResourceNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch(ForbiddenException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
        catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }

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
}
