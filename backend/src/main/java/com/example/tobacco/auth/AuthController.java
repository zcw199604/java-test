package com.example.tobacco.auth;

import com.example.tobacco.common.result.ApiResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/captcha")
    public ApiResponse<Map<String, String>> captcha() {
        return ApiResponse.success(authService.createCaptcha());
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Validated @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success(authService.login(request, httpRequest));
    }

    @GetMapping("/profile")
    public ApiResponse<LoginResponse> profile(@RequestAttribute("username") String username, @RequestAttribute("token") String token) {
        return ApiResponse.success(authService.currentProfile(username, token));
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestAttribute("token") String token) {
        authService.logout(token);
        return ApiResponse.success("ok");
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Map<String, String>> forgotPassword(@Validated @RequestBody ForgotPasswordRequest request) {
        return ApiResponse.success(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@Validated @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success("ok");
    }
}
