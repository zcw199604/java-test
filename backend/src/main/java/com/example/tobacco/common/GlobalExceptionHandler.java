package com.example.tobacco.common;

import com.example.tobacco.common.result.ApiResponse;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ApiResponse.failure(400, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<String> handleValidation(MethodArgumentNotValidException ex) {
        return ApiResponse.failure(400, ex.getBindingResult().getFieldError().getDefaultMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ApiResponse<String> handleAuthentication(AuthenticationException ex) {
        return ApiResponse.failure(401, ex.getMessage());
    }

    @ExceptionHandler(AuthorizationException.class)
    public ApiResponse<String> handleAuthorization(AuthorizationException ex) {
        return ApiResponse.failure(403, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<String> handleException(Exception ex) {
        return ApiResponse.failure(500, ex.getMessage());
    }
}
