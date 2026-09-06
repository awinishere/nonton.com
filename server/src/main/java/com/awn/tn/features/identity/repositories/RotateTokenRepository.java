package com.awn.tn.features.identity.repositories;

import com.awn.tn.features.identity.domain.RotateToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RotateTokenRepository extends JpaRepository<RotateToken, UUID> {

    Optional<RotateToken> findByRefreshToken(String refreshToken);

    Optional<RotateToken> findByCredentialId(UUID credentialId);

    @Query("SELECT r FROM RotateToken r JOIN FETCH r.credential c JOIN FETCH c.users WHERE r.refreshToken = :refreshToken")
    Optional<RotateToken> findByRefreshTokenWithDetails(@Param("refreshToken") String refreshToken);

    void deleteByCredentialId(UUID credentialId);
}