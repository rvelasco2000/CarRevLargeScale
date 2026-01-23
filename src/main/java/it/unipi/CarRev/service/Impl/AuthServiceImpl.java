package it.unipi.CarRev.service.Impl;

import it.unipi.CarRev.config.JwtService;
import it.unipi.CarRev.config.RedisConfig;
import it.unipi.CarRev.dao.mongo.UserDAO;
import it.unipi.CarRev.dao.redis.RefreshTokenDAO;
import it.unipi.CarRev.dto.LoginResponse;
import it.unipi.CarRev.model.User;
import it.unipi.CarRev.service.AuthService;
import it.unipi.CarRev.utils.UtilsForDate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {
    private final BotDetectionService botDetectionService;
    private final UserDAO userDAO;
    private final RefreshTokenDAO refreshTokenDAO;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(
            UserDAO userDAO,
            RefreshTokenDAO refreshTokenDAO,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            BotDetectionService botDetectionService) {
        this.userDAO = userDAO;
        this.refreshTokenDAO = refreshTokenDAO;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.botDetectionService=botDetectionService;
    }

    @Override
    public LoginResponse register(String username, String email, String password) {
        if (userDAO.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        // (optional but recommended) avoid duplicate emails
        if (email != null && userDAO.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setAdmin(false);

        user = userDAO.save(user);
        updateRegisteredUser();
        return issueTokens(user);
    }
    private void updateRegisteredUser(){
        try(Jedis jedis= RedisConfig.getJedis()){
            String key="TrafficLog:"+ UtilsForDate.getDate()+":nOfRegisteredUser";
            jedis.incr(key);
        }
        catch(Exception e){
            System.err.println("Error during the update of the number of registered user in redis");
        }
    }

    @Override
    public LoginResponse login(String username, String password,String ip) {
        User user = userDAO.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        botDetectionService.mapIpToUsername(user.getUsername(),ip);

        return issueTokens(user);
    }

    @Override
    public LoginResponse refresh(String refreshToken) {
        // Validate refresh token + check it is not revoked (Redis)
        String jti = jwtService.extractJti(refreshToken);
        String usernameFromRedis = refreshTokenDAO.getUsername(jti);
        if (usernameFromRedis == null) {
            throw new RuntimeException("Refresh token revoked/expired");
        }

        String usernameFromToken = jwtService.extractUsername(refreshToken);
        if (!usernameFromToken.equals(usernameFromRedis)) {
            throw new RuntimeException("Refresh token invalid");
        }

        User user = userDAO.findByUsername(usernameFromToken)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String newAccess = jwtService.generateAccessToken(
                user.getUsername(),
                Map.of("isAdmin", user.isAdmin())
        );

        // Keep same refresh token (simple approach)
        return new LoginResponse(newAccess, refreshToken);
    }

    @Override
    public void logout(String refreshToken) {
        String jti = jwtService.extractJti(refreshToken);
        refreshTokenDAO.revoke(jti);
    }

    private LoginResponse issueTokens(User user) {
        String access = jwtService.generateAccessToken(
                user.getUsername(),
                Map.of("isAdmin", user.isAdmin())
        );

        String refresh = jwtService.generateRefreshToken(user.getUsername());
        String jti = jwtService.extractJti(refresh);

        refreshTokenDAO.store(jti, user.getUsername());

        return new LoginResponse(access, refresh);
    }
}
