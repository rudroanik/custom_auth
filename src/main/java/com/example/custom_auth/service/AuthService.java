package com.example.custom_auth.service;

import com.example.custom_auth.dto.AuthResponse;
import com.example.custom_auth.dto.LoginRequest;
import com.example.custom_auth.dto.RegisterRequest;
import com.example.custom_auth.entity.User;
import com.example.custom_auth.exception.EmailAlreadyExistsException;
import com.example.custom_auth.exception.UserNotVerifiedException;
import com.example.custom_auth.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final VerificationService verificationService;
    private final EmailService emailService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        // Create user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .verified(false)
                .enabled(true)
                .build();

        userRepository.save(user);
        log.info("User registered: {}", user.getEmail());

        // Create and send verification token
        String token = verificationService.createVerificationToken(user);
        emailService.sendVerificationEmail(user.getEmail(), token);

        return AuthResponse.builder()
                .email(user.getEmail())
                .message("Registration successful. Please check your email to verify your account.")
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Check if user is verified
        if (!user.getVerified()) {
            log.warn("Login attempt by unverified user: {}", user.getEmail());

            // Check if user can receive a new verification email
            verificationService.checkResendEligibility(user);

            // Send new verification email
            String token = verificationService.createVerificationToken(user);
            emailService.sendVerificationEmail(user.getEmail(), token);

            throw new UserNotVerifiedException(
                    "Email not verified. A new verification link has been sent to your email."
            );
        }

        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Generate JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String jwtToken = jwtService.generateToken(userDetails);

        log.info("User logged in successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .message("Login successful")
                .build();
    }

    @Transactional
    public void verifyEmail(String token) {
        // Validate token
        verificationService.validateAndUseToken(token);

        // Get user and mark as verified
        User user = verificationService.getUserFromToken(token);
        user.setVerified(true);
        userRepository.save(user);

        log.info("User email verified: {}", user.getEmail());
    }
}
