package com.awn.tn.identity.application.login;

import com.awn.tn.identity.domain.UsersCredentials;
import com.awn.tn.identity.domain.UsersProfile;
import com.awn.tn.identity.domain.extensions.RoleType;
import com.awn.tn.identity.infrastructure.persistence.UsersCredentialsRepository;
import com.awn.tn.identity.infrastructure.security.JwtService;
import com.awn.tn.identity.utils.GenerateHash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTests {

    @Mock
    private UsersCredentialsRepository usersCredentialsRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private UsersCredentials credentials;

    @Mock
    private UsersProfile profile;

    @InjectMocks
    private LoginService loginService;

    private LoginRequest request;
    private UUID userId;
    private RoleType role;
    private String passwordHash;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        role = RoleType.USERS;
        String password = "password123";
        passwordHash = GenerateHash.generate(password);

        request = new LoginRequest(
                "user@example.com",
                password
        );
    }

    @Test
    void shouldLoginSuccessfully() {
        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        when(usersCredentialsRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(credentials));

        when(credentials.getProfile())
                .thenReturn(profile);

        when(profile.getId())
                .thenReturn(userId);

        when(credentials.getRole())
                .thenReturn(role);

        when(credentials.getPassword())
                .thenReturn(passwordHash);

        when(jwtService.generateAccessToken(userId, role))
                .thenReturn(accessToken);

        when(jwtService.generateRefreshToken(userId, role))
                .thenReturn(refreshToken);

        LoginResponse response = loginService.login(request);

        assertNotNull(response);
        assertEquals(accessToken, response.accessToken());
        assertEquals(refreshToken, response.refreshToken());

        verify(usersCredentialsRepository)
                .findByEmail(request.email());

        verify(jwtService)
                .generateAccessToken(userId, role);

        verify(jwtService)
                .generateRefreshToken(userId, role);
    }

    @Test
    void shouldRejectWhenEmailIsNotFound() {
        when(usersCredentialsRepository.findByEmail(request.email()))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> loginService.login(request)
                );

        assertEquals(
                "Invalid email",
                exception.getMessage()
        );

        verify(usersCredentialsRepository)
                .findByEmail(request.email());

        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldRejectWhenPasswordIsInvalid() {
        when(usersCredentialsRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(credentials));

        when(credentials.getPassword())
                .thenReturn(passwordHash);

        LoginRequest invalidRequest = new LoginRequest(
                request.email(),
                "wrong-password"
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> loginService.login(invalidRequest)
                );

        assertEquals(
                "Invalid password",
                exception.getMessage()
        );

        verify(usersCredentialsRepository)
                .findByEmail(request.email());

        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldPropagateExceptionWhenAccessTokenGenerationFails() {
        when(usersCredentialsRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(credentials));

        when(credentials.getProfile())
                .thenReturn(profile);

        when(profile.getId())
                .thenReturn(userId);

        when(credentials.getRole())
                .thenReturn(role);

        when(credentials.getPassword())
                .thenReturn(passwordHash);

        when(jwtService.generateAccessToken(userId, role))
                .thenThrow(
                        new IllegalStateException(
                                "Failed to generate JWT"
                        )
                );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> loginService.login(request)
                );

        assertEquals(
                "Failed to generate JWT",
                exception.getMessage()
        );

        verify(jwtService)
                .generateAccessToken(userId, role);

        verify(jwtService, never())
                .generateRefreshToken(any(), any());
    }

    @Test
    void shouldPropagateExceptionWhenRefreshTokenGenerationFails() {
        when(usersCredentialsRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(credentials));

        when(credentials.getProfile())
                .thenReturn(profile);

        when(profile.getId())
                .thenReturn(userId);

        when(credentials.getRole())
                .thenReturn(role);

        when(credentials.getPassword())
                .thenReturn(passwordHash);

        when(jwtService.generateAccessToken(userId, role))
                .thenReturn("access-token");

        when(jwtService.generateRefreshToken(userId, role))
                .thenThrow(
                        new IllegalStateException(
                                "Failed to generate JWT"
                        )
                );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> loginService.login(request)
                );

        assertEquals(
                "Failed to generate JWT",
                exception.getMessage()
        );

        verify(jwtService)
                .generateAccessToken(userId, role);

        verify(jwtService)
                .generateRefreshToken(userId, role);
    }
}