package it.unipi.CarRev.service;

import it.unipi.CarRev.dto.LoginResponse;

public interface AuthService {

    LoginResponse register(String username, String email, String password);

    LoginResponse login(String username, String password,String ip);

    LoginResponse refresh(String refreshToken);

    void logout(String refreshToken);
}
