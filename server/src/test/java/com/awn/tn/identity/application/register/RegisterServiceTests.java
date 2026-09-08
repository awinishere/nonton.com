package com.awn.tn.identity.application.register;

import com.awn.tn.identity.domain.UsersCredentials;
import com.awn.tn.identity.domain.UsersProfile;
import com.awn.tn.identity.domain.extensions.RoleType;
import com.awn.tn.identity.infrastructure.persistence.UsersCredentialsRepository;
import com.awn.tn.identity.infrastructure.persistence.UsersProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTests {

    @Mock
    private UsersProfileRepository usersProfileRepository;

    @Mock
    private UsersCredentialsRepository usersCredentialsRepository;

    @InjectMocks
    private RegisterService registerService;

    private RegisterRequest request;

    @BeforeEach
    void setUp() {
        request = new RegisterRequest(
                "John Doe",
                "john@example.com",
                "password123"
        );
    }

    @Test
    void shouldRegisterSuccessfully() {
        UUID profileId = UUID.randomUUID();

        when(usersCredentialsRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(usersProfileRepository.save(any(UsersProfile.class)))
                .thenAnswer(invocation -> {
                    UsersProfile profile = invocation.getArgument(0);
                    profile.setId(profileId);
                    return profile;
                });

        when(usersCredentialsRepository.save(any(UsersCredentials.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegisterResponse response =
                registerService.register(request);

        assertNotNull(response);
        assertEquals(profileId, response.userId());
        assertEquals(request.name(), response.name());
        assertEquals(request.email(), response.email());

        verify(usersCredentialsRepository)
                .existsByEmail(request.email());

        verify(usersProfileRepository)
                .save(any(UsersProfile.class));

        verify(usersCredentialsRepository)
                .save(any(UsersCredentials.class));
    }

    @Test
    void shouldRejectWhenEmailAlreadyRegistered() {
        when(usersCredentialsRepository.existsByEmail(request.email()))
                .thenReturn(true);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> registerService.register(request)
                );

        assertEquals(
                "Email already registered",
                exception.getMessage()
        );

        verify(usersCredentialsRepository)
                .existsByEmail(request.email());

        verifyNoInteractions(usersProfileRepository);

        verify(
                usersCredentialsRepository,
                never()
        ).save(any(UsersCredentials.class));
    }

    @Test
    void shouldCreateProfileWithCorrectData() {
        UUID profileId = UUID.randomUUID();

        when(usersCredentialsRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(usersProfileRepository.save(any(UsersProfile.class)))
                .thenAnswer(invocation -> {
                    UsersProfile profile = invocation.getArgument(0);
                    profile.setId(profileId);
                    return profile;
                });

        when(usersCredentialsRepository.save(any(UsersCredentials.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        registerService.register(request);

        ArgumentCaptor<UsersProfile> captor =
                ArgumentCaptor.forClass(UsersProfile.class);

        verify(usersProfileRepository)
                .save(captor.capture());

        UsersProfile profile = captor.getValue();

        assertEquals(request.name(), profile.getName());
        assertEquals(profileId, profile.getId());
    }

    @Test
    void shouldCreateCredentialsWithCorrectData() {
        UUID profileId = UUID.randomUUID();

        when(usersCredentialsRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(usersProfileRepository.save(any(UsersProfile.class)))
                .thenAnswer(invocation -> {
                    UsersProfile profile = invocation.getArgument(0);
                    profile.setId(profileId);
                    return profile;
                });

        when(usersCredentialsRepository.save(any(UsersCredentials.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        registerService.register(request);

        ArgumentCaptor<UsersCredentials> captor =
                ArgumentCaptor.forClass(UsersCredentials.class);

        verify(usersCredentialsRepository)
                .save(captor.capture());

        UsersCredentials credentials = captor.getValue();

        assertEquals(request.email(), credentials.getEmail());
        assertEquals(RoleType.USERS, credentials.getRole());
        assertNotEquals(
                request.password(),
                credentials.getPassword()
        );

        assertNotNull(credentials.getPassword());
        assertEquals(
                profileId,
                credentials.getProfile().getId()
        );
    }
}