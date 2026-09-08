package com.awn.tn.identity.application.login;

import com.awn.tn.identity.domain.UsersCredentials;
import com.awn.tn.identity.infrastructure.persistence.UsersCredentialsRepository;
import com.awn.tn.identity.infrastructure.security.JwtService;
import com.awn.tn.identity.utils.GenerateHash;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UsersCredentialsRepository usersCredentialsRepository;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request){
        UsersCredentials credentials = usersCredentialsRepository
                .findByEmail(request.email())
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid email"));

        if (!GenerateHash.verify(
                request.password(),
                credentials.getPassword()
        )){
            throw new IllegalStateException("Invalid password");
        }

        String accessToken = jwtService.generateAccessToken(
                credentials.getProfile().getId(),
                credentials.getRole()
        );

        String refreshToken = jwtService.generateRefreshToken(
                credentials.getProfile().getId(),
                credentials.getRole()

        );

        return new LoginResponse(
                accessToken,
                refreshToken
        );
    }
}
