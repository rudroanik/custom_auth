package com.example.custom_auth.service;


import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Date;


public interface JwtService {

    /**
     * Generates a JWT access token for the user.
     *
     * @param userDetails the authenticated user
     * @return the signed JWT token string
     */
    String generateToken(UserDetails userDetails);

    /**
     * Extracts the username (subject) from a JWT token.
     *
     * @param token the JWT token
     * @return the username
     */
    String extractUsername(String token);

    /**
     * Extracts the expiration date from a JWT token.
     *
     * @param token the JWT token
     * @return the expiration date
     */
    Date extractExpiration(String token);

    /**
     * Validates a JWT token against user details.
     *
     * @param token the JWT token to validate
     * @param userDetails the user to validate against
     * @return true if token is valid and not expired, false otherwise
     */
    boolean isTokenValid(String token, UserDetails userDetails);
}