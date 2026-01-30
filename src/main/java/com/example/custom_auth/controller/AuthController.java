package com.example.custom_auth.controller;

import com.example.custom_auth.dto.ApiResponse;
import com.example.custom_auth.dto.AuthResponse;
import com.example.custom_auth.dto.LoginRequest;
import com.example.custom_auth.dto.RegisterRequest;
import com.example.custom_auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestParam("token") String token) {
        log.info("Email verification request received");
        authService.verifyEmail(token);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Email verified successfully! You can now login.")
                .build();

        return ResponseEntity.ok(response);
    }
}
