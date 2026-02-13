package it.unipi.CarRev.controller;

import it.unipi.CarRev.dto.ChangeEmailRequest;
import it.unipi.CarRev.dto.ChangePasswordRequest;
import it.unipi.CarRev.dto.ReviewUpdateRequestDTO;
import it.unipi.CarRev.service.Impl.DeleteReviewServiceImplementation;
import it.unipi.CarRev.service.Impl.DeleteUserServiceImplementation;
import it.unipi.CarRev.service.Impl.UpdateReviewServiceImpl;
import it.unipi.CarRev.service.exception.BadRequestException;
import it.unipi.CarRev.service.Impl.UserUpdateServiceImpl;
import it.unipi.CarRev.service.exception.ForbiddenException;
import it.unipi.CarRev.service.exception.ResourceNotFoundException;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final DeleteReviewServiceImplementation deleteReviewServiceImplementation;
    private final UpdateReviewServiceImpl updateReviewService;
    private final DeleteUserServiceImplementation deleteUserServiceImplementation;
    //public UserController(DeleteReviewServiceImplementation deleteReviewServiceImplementation,UpdateReviewServiceImpl updateReviewService,DeleteUserServiceImplementation deleteUserServiceImplementation){
    private final UserUpdateServiceImpl userUpdateService;
    public UserController(DeleteReviewServiceImplementation deleteReviewServiceImplementation,UpdateReviewServiceImpl updateReviewService,DeleteUserServiceImplementation deleteUserServiceImplementation,UserUpdateServiceImpl userUpdateServiceImpl){
        this.deleteReviewServiceImplementation=deleteReviewServiceImplementation;
        this.updateReviewService=updateReviewService;
        this.deleteUserServiceImplementation=deleteUserServiceImplementation;
        this.userUpdateService=userUpdateServiceImpl;
    }
    @GetMapping("/deleteAccount")
    public ResponseEntity<String> deleteAccount(){
        try{
            deleteUserServiceImplementation.deleteAUser();
            return ResponseEntity.ok("account correctly deleted");
        }
        catch (ForbiddenException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
        catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error during the delete of the account");
        }
    }
    @PostMapping("/updateReview")
    public ResponseEntity<String> updateReview(@Valid @RequestBody ReviewUpdateRequestDTO request){
        try{
            updateReviewService.updateReview(request);
            return ResponseEntity.ok("review correctly updated");
        }
        catch (BadRequestException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
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

    @PostMapping("/changeEmail")
    public ResponseEntity<String> changeEmail(@Valid @RequestBody ChangeEmailRequest request, Authentication authentication){
        try{
            if(authentication==null || !authentication.isAuthenticated()){
                throw new RuntimeException("User must be authenticated");
            }
            userUpdateService.changeEmail(authentication.getName(), request.getCurrentPassword(), request.getNewEmail());
            return ResponseEntity.ok("email correctly updated");
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

    @PostMapping("/changePassword")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request, Authentication authentication){
        try{
            if(authentication==null || !authentication.isAuthenticated()){
                throw new RuntimeException("User must be authenticated");
            }
            userUpdateService.changePassword(authentication.getName(), request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok("password correctly updated");
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
}
