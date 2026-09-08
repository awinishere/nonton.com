package com.awn.tn.identity.application.register;

import com.awn.tn.identity.domain.UsersCredentials;
import com.awn.tn.identity.domain.UsersProfile;
import com.awn.tn.identity.domain.extensions.RoleType;
import com.awn.tn.identity.infrastructure.persistence.UsersCredentialsRepository;
import com.awn.tn.identity.infrastructure.persistence.UsersProfileRepository;
import com.awn.tn.identity.utils.GenerateHash;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterService {
    private final UsersProfileRepository usersProfileRepository;
    private final UsersCredentialsRepository usersCredentialsRepository;

    @Transactional
    public RegisterResponse register(RegisterRequest request){
        if (usersCredentialsRepository.existsByEmail(request.email())){
            throw new IllegalStateException("Email already registered");
        }

        UsersProfile profile = new UsersProfile();
        profile.setName(request.name());
        usersProfileRepository.save(profile);

        UsersCredentials credentials = new UsersCredentials();
        credentials.setProfile(profile);
        credentials.setEmail(request.email());
        credentials.setPassword(GenerateHash.generate(request.password()));
        credentials.setRole(RoleType.USERS);

        usersCredentialsRepository.save(credentials);
        return new RegisterResponse(
                profile.getId(),
                profile.getName(),
                credentials.getEmail()
        );
    }
}
