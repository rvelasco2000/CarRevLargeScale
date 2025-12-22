package it.unipi.CarRev.service;

import it.unipi.CarRev.dto.LoginResponse;

public interface AuthService {

    LoginResponse register(String username, String email, String password);

    LoginResponse login(String username, String password);

    LoginResponse refresh(String refreshToken);

    void logout(String refreshToken);
}
