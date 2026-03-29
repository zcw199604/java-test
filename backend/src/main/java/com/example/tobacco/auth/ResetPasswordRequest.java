package com.example.tobacco.auth;

import com.fasterxml.jackson.annotation.JsonAlias;

import javax.validation.constraints.NotBlank;

public class ResetPasswordRequest {
    @NotBlank private String username;
    @NotBlank @JsonAlias({"token"}) private String resetToken;
    @NotBlank private String newPassword;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
