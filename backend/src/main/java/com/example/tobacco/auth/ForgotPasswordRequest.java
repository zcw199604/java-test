package com.example.tobacco.auth;

import javax.validation.constraints.NotBlank;

public class ForgotPasswordRequest {
    @NotBlank private String username;
    @NotBlank private String captchaKey;
    @NotBlank private String captchaCode;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getCaptchaKey() { return captchaKey; }
    public void setCaptchaKey(String captchaKey) { this.captchaKey = captchaKey; }
    public String getCaptchaCode() { return captchaCode; }
    public void setCaptchaCode(String captchaCode) { this.captchaCode = captchaCode; }
}
