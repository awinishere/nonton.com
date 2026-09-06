package com.awn.tn.features.identity.repositories;

import com.awn.tn.features.identity.domain.Otps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpsRepository extends JpaRepository<Otps, UUID> {

    @Query("""
        SELECT o FROM Otps o 
        WHERE o.user.id = :userId 
          AND o.purpose = :purpose 
          AND o.isUsed = false 
          AND o.expiresAt > :now 
        ORDER BY o.createdAt DESC
    """)
    Optional<Otps> findLatestActiveOtp(
            @Param("userId") UUID userId,
            @Param("purpose") String purpose,
            @Param("now") LocalDateTime now
    );

    @Modifying
    @Query("DELETE FROM Otps o WHERE o.user.id = :userId AND o.purpose = :purpose")
    void deleteByUserIdAndPurpose(@Param("userId") UUID userId, @Param("purpose") String purpose);

    @Modifying
    @Query("DELETE FROM Otps o WHERE o.expiresAt <= :now OR o.isUsed = true")
    void deleteExpiredOrUsedOtps(@Param("now") LocalDateTime now);
}
