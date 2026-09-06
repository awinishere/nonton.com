package com.awn.tn.identity.repositories;

import com.awn.tn.identity.domain.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CredentialsRepository extends JpaRepository<Credentials, UUID> {

    Optional<Credentials> findByEmail(String email);

    @Query("SELECT c FROM Credentials c JOIN FETCH c.users WHERE c.email = :email")
    Optional<Credentials> findByEmailWithUsers(@Param("email") String email);

    boolean existsByEmail(String email);

    Optional<Credentials> findByAccessToken(String accessToken);

    Optional<Credentials> findByUsersId(UUID userId);
}