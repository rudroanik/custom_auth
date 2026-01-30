package com.example.custom_auth.service;

import com.example.custom_auth.entity.User;
import com.example.custom_auth.entity.VerificationToken;
import com.example.custom_auth.exception.VerificationException;
import com.example.custom_auth.repository.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationService {

    private final VerificationTokenRepository verificationTokenRepository;

    @Value("${app.verification.expiration-minutes}")
    private int expirationMinutes;

    @Value("${app.verification.resend-interval-minutes}")
    private int resendIntervalMinutes;

    @Transactional
    public String createVerificationToken(User user) {
        // Invalidate all previous tokens for this user
        verificationTokenRepository.invalidateAllTokensForUser(user);

        // Generate new token
        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(expirationMinutes))
                .used(false)
                .lastSentAt(LocalDateTime.now())
                .build();

        verificationTokenRepository.save(verificationToken);
        log.info("Created verification token for user: {}", user.getEmail());

        return token;
    }

    @Transactional
    public void validateAndUseToken(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new VerificationException("Invalid verification token"));

        if (verificationToken.getUsed()) {
            throw new VerificationException("Verification token has already been used");
        }

        if (verificationToken.isExpired()) {
            throw new VerificationException("Verification token has expired");
        }

        // Mark token as used
        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);

        log.info("Token validated and marked as used for user: {}", verificationToken.getUser().getEmail());
    }

    public void checkResendEligibility(User user) {
        Optional<VerificationToken> existingToken = verificationTokenRepository.findByUserAndUsedFalse(user);

        if (existingToken.isPresent()) {
            LocalDateTime lastSent = existingToken.get().getLastSentAt();
            LocalDateTime now = LocalDateTime.now();

            long minutesSinceLastSent = java.time.Duration.between(lastSent, now).toMinutes();

            if (minutesSinceLastSent < resendIntervalMinutes) {
                long remainingMinutes = resendIntervalMinutes - minutesSinceLastSent;
                throw new VerificationException(
                        String.format("Please wait %d more minute(s) before requesting a new verification email",
                                remainingMinutes)
                );
            }
        }
    }

    public User getUserFromToken(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new VerificationException("Invalid verification token"));
        return verificationToken.getUser();
    }
}

