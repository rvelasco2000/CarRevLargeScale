package it.unipi.CarRev.controller;


import it.unipi.CarRev.dto.UserInfoResponseDTO;
import it.unipi.CarRev.dto.UserReviewResponseDTO;
import it.unipi.CarRev.service.Impl.ShowReviewOfAUserServiceImplementation;
import it.unipi.CarRev.service.Impl.ShowUserInfoServiceImplementation;
import it.unipi.CarRev.service.exception.ResourceNotFoundException;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/general")
public class GeneralController {
    private final ShowReviewOfAUserServiceImplementation showReviewOfAUserServiceImplementation;
    private final ShowUserInfoServiceImplementation showUserInfoServiceImplementation;
    public GeneralController(ShowReviewOfAUserServiceImplementation showReviewOfAUserServiceImplementation, ShowUserInfoServiceImplementation showUserInfoServiceImplementation){
        this.showReviewOfAUserServiceImplementation=showReviewOfAUserServiceImplementation;
        this.showUserInfoServiceImplementation=showUserInfoServiceImplementation;
    }
    @GetMapping("/userRev")
    public ResponseEntity<?> getUserReviews(@NotNull @RequestParam String username,@NotNull @RequestParam Integer numPage){
        try{
            Page<UserReviewResponseDTO> userReviewResponseDTOS=showReviewOfAUserServiceImplementation.getAllReviewsFromUsers(username,numPage);
            return ResponseEntity.ok(userReviewResponseDTOS);

        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch(RuntimeException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("an error has occurred while fetching the user reviews");
        }
    }
    @GetMapping("/userInfo")
    public ResponseEntity<?> getUserInfo(@NotNull @RequestParam String username){
        try{
            UserInfoResponseDTO dto=showUserInfoServiceImplementation.getUserInfo(username);
            return ResponseEntity.ok(dto);
        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch(RuntimeException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("an error has occurred while fetching the user info");
        }

    }


}
