package it.unipi.CarRev.service.Impl;

import it.unipi.CarRev.dao.mongo.UserDAO;
import it.unipi.CarRev.model.User;
import it.unipi.CarRev.service.exception.ForbiddenException;
import it.unipi.CarRev.service.exception.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserUpdateServiceImpl {
    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;

    public UserUpdateServiceImpl(UserDAO userDAO, PasswordEncoder passwordEncoder) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
    }

    public void changeEmail(String username, String currentPassword, String newEmail) {
        User user = userDAO.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new ForbiddenException("Invalid credentials");
        }

        if (newEmail == null || newEmail.isBlank()) {
            throw new RuntimeException("Email cannot be empty");
        }

        if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(newEmail)) {
            return;
        }

        if (userDAO.existsByEmail(newEmail)) {
            throw new RuntimeException("Email already exists");
        }

        user.setEmail(newEmail);
        userDAO.save(user);
    }

    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = userDAO.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new ForbiddenException("Invalid credentials");
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException("New password cannot be empty");
        }

        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new RuntimeException("New password must be different from the current password");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userDAO.save(user);
    }
}
