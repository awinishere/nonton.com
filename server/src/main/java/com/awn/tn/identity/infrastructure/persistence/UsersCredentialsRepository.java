package com.awn.tn.identity.infrastructure.persistence;

import com.awn.tn.identity.domain.UsersCredentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsersCredentialsRepository extends JpaRepository<UsersCredentials, UUID> {

    Optional<UsersCredentials> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<UsersCredentials> findByProfile_Id(UUID profileId);
}