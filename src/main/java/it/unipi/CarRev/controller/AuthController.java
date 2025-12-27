package it.unipi.CarRev.controller;

import it.unipi.CarRev.dto.LoginRequest;
import it.unipi.CarRev.dto.LoginResponse;
import it.unipi.CarRev.dto.RefreshRequest;
import it.unipi.CarRev.dto.RegisterRequest;
import it.unipi.CarRev.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        // IMPORTANT: AuthService must have register(username, email, password)
        return ResponseEntity.ok(
                authService.register(req.getUsername(), req.getEmail(), req.getPassword())
        );
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(
                authService.login(req.getUsername(), req.getPassword())
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        return ResponseEntity.ok(
                authService.refresh(req.getRefreshToken())
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest req) {
        authService.logout(req.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
