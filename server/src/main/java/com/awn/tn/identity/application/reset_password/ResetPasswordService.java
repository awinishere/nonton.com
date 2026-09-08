package com.awn.tn.identity.application.reset_password;

import com.awn.tn.identity.domain.UsersCredentials;
import com.awn.tn.identity.infrastructure.persistence.UsersCredentialsRepository;
import com.awn.tn.identity.utils.GenerateHash;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResetPasswordService {

    private final UsersCredentialsRepository credentialsRepository;

    public void resetPassword(
            UUID userId,
            ResetPasswordRequest request
    ) {
        UsersCredentials credentials = credentialsRepository
                .findByProfile_Id(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User credentials not found"
                        )
                );

        if (!GenerateHash.verify(
                request.currentPassword(),
                credentials.getPassword()
        )) {
            throw new IllegalArgumentException(
                    "Current password is incorrect"
            );
        }

        credentials.setPassword(
                GenerateHash.generate(request.password())
        );

        credentialsRepository.save(credentials);
    }
}