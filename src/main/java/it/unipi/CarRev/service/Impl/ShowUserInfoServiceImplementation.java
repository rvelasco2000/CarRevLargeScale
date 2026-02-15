package it.unipi.CarRev.service.Impl;


import it.unipi.CarRev.dao.mongo.UserDAO;
import it.unipi.CarRev.dto.UserInfoResponseDTO;
import it.unipi.CarRev.model.User;
import it.unipi.CarRev.service.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ShowUserInfoServiceImplementation {
    private final UserDAO userDAO;
    public ShowUserInfoServiceImplementation(UserDAO userDAO){
        this.userDAO=userDAO;
    }
    public UserInfoResponseDTO getUserInfo(String username){
        UserInfoResponseDTO returnDto=new UserInfoResponseDTO();
        User user=userDAO.findByUsername(username).orElse(null);
        if(user==null){
            throw new ResourceNotFoundException("the user does not exists");
        }
        returnDto.setUsername(user.getUsername());
        returnDto.setEmail(user.getEmail());
        return returnDto;
    }
}
