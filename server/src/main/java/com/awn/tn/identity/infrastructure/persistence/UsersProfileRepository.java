package com.awn.tn.identity.infrastructure.persistence;

import com.awn.tn.identity.domain.UsersProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UsersProfileRepository extends JpaRepository<UsersProfile, UUID> {
}