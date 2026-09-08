package com.awn.tn.identity.infrastructure.security;

import com.awn.tn.identity.domain.extensions.RoleType;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final String secret;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${security.jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        this.secret = secret;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    private String generateToken(
            UUID userId,
            RoleType role,
            long expiration,
            String type
    ) {
        try {
            Instant now = Instant.now();

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(userId.toString())
                    .claim("type", type)
                    .claim("role", role.name())
                    .jwtID(UUID.randomUUID().toString())
                    .issueTime(Date.from(now))
                    .expirationTime(
                            Date.from(now.plusSeconds(expiration))
                    )
                    .build();

            SignedJWT jwt = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claims
            );

            jwt.sign(new MACSigner(secret));

            return jwt.serialize();

        } catch (JOSEException e) {
            throw new IllegalStateException(
                    "Failed to generate JWT",
                    e
            );
        }
    }

    public String generateAccessToken(
            UUID userId,
            RoleType role
    ) {
        return generateToken(
                userId,
                role,
                accessTokenExpiration,
                "access"
        );
    }

    public String generateRefreshToken(
            UUID userId,
            RoleType role
    ) {
        return generateToken(
                userId,
                role,
                refreshTokenExpiration,
                "refresh"
        );
    }

    public JWTClaimsSet parseAndVerify(
            String token
    ) throws ParseException, JOSEException {

        SignedJWT jwt = SignedJWT.parse(token);

        if (!jwt.verify(new MACVerifier(secret))) {
            throw new IllegalArgumentException(
                    "Invalid JWT signature"
            );
        }

        JWTClaimsSet claims = jwt.getJWTClaimsSet();

        if (claims.getExpirationTime() == null ||
                claims.getExpirationTime().before(new Date())) {

            throw new IllegalArgumentException(
                    "JWT expired"
            );
        }

        return claims;
    }
}