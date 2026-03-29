package com.example.tobacco.auth;

import com.fasterxml.jackson.annotation.JsonAlias;

import javax.validation.constraints.NotBlank;

public class ForgotPasswordRequest {
    @NotBlank private String username;
    @NotBlank @JsonAlias({"captchaUuid", "uuid", "key"}) private String captchaKey;
    @NotBlank @JsonAlias({"code", "captcha"}) private String captchaCode;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getCaptchaKey() { return captchaKey; }
    public void setCaptchaKey(String captchaKey) { this.captchaKey = captchaKey; }
    public String getCaptchaCode() { return captchaCode; }
    public void setCaptchaCode(String captchaCode) { this.captchaCode = captchaCode; }
}
