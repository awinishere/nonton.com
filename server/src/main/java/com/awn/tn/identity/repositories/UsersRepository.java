package com.awn.tn.identity.repositories;

import com.awn.tn.identity.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsersRepository extends JpaRepository<Users, UUID> {

    @Query("SELECT u FROM Users u LEFT JOIN FETCH u.credentials WHERE u.id = :id")
    Optional<Users> findByIdWithCredentials(@Param("id") UUID id);

    @Query("SELECT u FROM Users u JOIN u.credentials c WHERE c.email = :email")
    Optional<Users> findByEmail(@Param("email")String email);

    boolean existsByIdAndStatusTrue(UUID id);
}
