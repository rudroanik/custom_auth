package com.example.custom_auth.repository;

import com.example.custom_auth.entity.User;
import com.example.custom_auth.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUserAndUsedFalse(User user);

    @Modifying
    @Query("UPDATE VerificationToken vt SET vt.used = true WHERE vt.user = :user AND vt.used = false")
    void invalidateAllTokensForUser(User user);
}
