package it.unipi.CarRev.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ChangeEmailRequest {
    @NotBlank
    @Email
    private String newEmail;

    @NotBlank
    private String currentPassword;

    public ChangeEmailRequest() {}

    public String getNewEmail() {
        return newEmail;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }
}
