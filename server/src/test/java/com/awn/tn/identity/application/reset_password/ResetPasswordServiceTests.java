package com.awn.tn.identity.application.reset_password;

import com.awn.tn.identity.domain.UsersCredentials;
import com.awn.tn.identity.infrastructure.persistence.UsersCredentialsRepository;
import com.awn.tn.identity.utils.GenerateHash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResetPasswordServiceTests {

    @Mock
    private UsersCredentialsRepository credentialsRepository;

    @Mock
    private UsersCredentials credentials;

    @InjectMocks
    private ResetPasswordService resetPasswordService;

    private UUID userId;
    private String currentPassword;
    private String newPassword;
    private String currentPasswordHash;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        currentPassword = "oldPassword123";
        newPassword = "newPassword123";
        currentPasswordHash = GenerateHash.generate(currentPassword);
    }

    @Test
    void shouldResetPasswordSuccessfully() {
        ResetPasswordRequest request = new ResetPasswordRequest(
                currentPassword,
                newPassword
        );

        when(credentialsRepository.findByProfile_Id(userId))
                .thenReturn(Optional.of(credentials));

        when(credentials.getPassword())
                .thenReturn(currentPasswordHash);

        resetPasswordService.resetPassword(
                userId,
                request
        );

        verify(credentials)
                .setPassword(anyString());

        verify(credentialsRepository)
                .save(credentials);
    }

    @Test
    void shouldRejectWhenUserCredentialsNotFound() {
        ResetPasswordRequest request = new ResetPasswordRequest(
                currentPassword,
                newPassword
        );

        when(credentialsRepository.findByProfile_Id(userId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> resetPasswordService.resetPassword(
                                userId,
                                request
                        )
                );

        assertEquals(
                "User credentials not found",
                exception.getMessage()
        );

        verify(credentialsRepository)
                .findByProfile_Id(userId);

        verifyNoInteractions(credentials);

        verify(
                credentialsRepository,
                never()
        ).save(any(UsersCredentials.class));
    }

    @Test
    void shouldRejectWhenCurrentPasswordIsIncorrect() {
        ResetPasswordRequest request = new ResetPasswordRequest(
                "wrongPassword",
                newPassword
        );

        when(credentialsRepository.findByProfile_Id(userId))
                .thenReturn(Optional.of(credentials));

        when(credentials.getPassword())
                .thenReturn(currentPasswordHash);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> resetPasswordService.resetPassword(
                                userId,
                                request
                        )
                );

        assertEquals(
                "Current password is incorrect",
                exception.getMessage()
        );

        verify(credentials)
                .getPassword();

        verify(credentials, never())
                .setPassword(anyString());

        verify(
                credentialsRepository,
                never()
        ).save(any(UsersCredentials.class));
    }

    @Test
    void shouldSaveNewPassword() {
        ResetPasswordRequest request = new ResetPasswordRequest(
                currentPassword,
                newPassword
        );

        when(credentialsRepository.findByProfile_Id(userId))
                .thenReturn(Optional.of(credentials));

        when(credentials.getPassword())
                .thenReturn(currentPasswordHash);

        resetPasswordService.resetPassword(
                userId,
                request
        );

        ArgumentCaptor<String> passwordCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(credentials)
                .setPassword(passwordCaptor.capture());

        String newPasswordHash =
                passwordCaptor.getValue();

        assertNotNull(newPasswordHash);
        assertNotEquals(
                newPassword,
                newPasswordHash
        );

        assertTrue(
                GenerateHash.verify(
                        newPassword,
                        newPasswordHash
                )
        );

        verify(credentialsRepository)
                .save(credentials);
    }
}