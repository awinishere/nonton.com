package com.awn.tn.identity.infrastructure.security;

import com.awn.tn.identity.domain.extensions.RoleType;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTests {

    private JwtService jwtService;

    private static final String SECRET =
            "this-is-a-test-secret-key-with-enough-length";

    private static final long ACCESS_TOKEN_EXPIRATION = 900;
    private static final long REFRESH_TOKEN_EXPIRATION = 604800;

    private UUID userId;
    private RoleType role;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                SECRET,
                ACCESS_TOKEN_EXPIRATION,
                REFRESH_TOKEN_EXPIRATION
        );

        userId = UUID.randomUUID();
        role = RoleType.USERS;
    }

    @Test
    void shouldGenerateValidAccessToken() throws Exception {

        String token = jwtService.generateAccessToken(
                userId,
                role
        );

        assertNotNull(token);
        assertFalse(token.isBlank());

        JWTClaimsSet claims =
                jwtService.parseAndVerify(token);

        assertEquals(
                userId.toString(),
                claims.getSubject()
        );

        assertEquals(
                "access",
                claims.getStringClaim("type")
        );

        assertEquals(
                role.name(),
                claims.getStringClaim("role")
        );
    }

    @Test
    void shouldGenerateValidRefreshToken() throws Exception {

        String token = jwtService.generateRefreshToken(
                userId,
                role
        );

        assertNotNull(token);
        assertFalse(token.isBlank());

        JWTClaimsSet claims =
                jwtService.parseAndVerify(token);

        assertEquals(
                userId.toString(),
                claims.getSubject()
        );

        assertEquals(
                "refresh",
                claims.getStringClaim("type")
        );

        assertEquals(
                role.name(),
                claims.getStringClaim("role")
        );
    }

    @Test
    void shouldVerifyValidToken() throws Exception {// GIVEN
        String token = jwtService.generateAccessToken(
                userId,
                role
        );


        JWTClaimsSet claims =
                jwtService.parseAndVerify(token);

        assertNotNull(claims);
        assertEquals(
                userId.toString(),
                claims.getSubject()
        );
        assertEquals(
                "access",
                claims.getStringClaim("type")
        );
        assertEquals(
                role.name(),
                claims.getStringClaim("role")
        );

        assertNotNull(claims.getJWTID());
        assertNotNull(claims.getIssueTime());
        assertNotNull(claims.getExpirationTime());

        assertTrue(
                claims.getExpirationTime()
                        .after(new Date())
        );
    }

    @Test
    void shouldRejectTokenWithInvalidSignature() throws Exception {
        JwtService anotherJwtService = new JwtService(
                "another-secret-key-with-enough-length",
                ACCESS_TOKEN_EXPIRATION,
                REFRESH_TOKEN_EXPIRATION
        );

        String token = anotherJwtService.generateAccessToken(
                userId,
                role
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> jwtService.parseAndVerify(token)
                );

        assertEquals(
                "Invalid JWT signature",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectExpiredToken() throws Exception {
        Instant now = Instant.now();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(userId.toString())
                .claim("type", "access")
                .claim("role", role.name())
                .jwtID(UUID.randomUUID().toString())
                .issueTime(Date.from(now.minusSeconds(60)))
                .expirationTime(Date.from(now.minusSeconds(1)))
                .build();

        SignedJWT jwt = createSignedJwt(claims);

        String expiredToken = jwt.serialize();

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> jwtService.parseAndVerify(expiredToken)
                );

        assertEquals(
                "JWT expired",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectMalformedToken() {
        String malformedToken =
                "this.is.not.a.valid.jwt";

        assertThrows(
                ParseException.class,
                () -> jwtService.parseAndVerify(
                        malformedToken
                )
        );

    }

    @Test
    void shouldRejectTokenWithTamperedPayload() throws Exception {
        String token = jwtService.generateAccessToken(
                userId,
                role
        );

        String[] parts = token.split("\\.");

        assertEquals(3, parts.length);

        String tamperedPayload =
                parts[1].substring(0, parts[1].length() - 1)
                        + (parts[1].endsWith("A") ? "B" : "A");

        String tamperedToken =
                parts[0]
                        + "."
                        + tamperedPayload
                        + "."
                        + parts[2];

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> jwtService.parseAndVerify(
                                tamperedToken
                        )
                );

        assertEquals(
                "Invalid JWT signature",
                exception.getMessage()
        );
    }

    @Test
    void shouldSetAccessTokenExpirationCorrectly()
            throws Exception {

        Instant beforeGeneration = Instant.now();

        String token = jwtService.generateAccessToken(
                userId,
                role
        );

        JWTClaimsSet claims =
                jwtService.parseAndVerify(token);

        Instant expiration =
                claims.getExpirationTime().toInstant();

        Instant expectedExpiration =
                beforeGeneration.plusSeconds(
                        ACCESS_TOKEN_EXPIRATION
                );

        long difference =
                Math.abs(
                        expiration.getEpochSecond()
                                - expectedExpiration.getEpochSecond()
                );

        assertTrue(
                difference <= 1,
                "Access token expiration should be approximately "
                        + ACCESS_TOKEN_EXPIRATION
                        + " seconds"
        );
    }

    @Test
    void shouldSetRefreshTokenExpirationCorrectly()
            throws Exception {

        // GIVEN
        Instant beforeGeneration = Instant.now();

        String token = jwtService.generateRefreshToken(
                userId,
                role
        );

        JWTClaimsSet claims =
                jwtService.parseAndVerify(token);

        Instant expiration =
                claims.getExpirationTime().toInstant();

        Instant expectedExpiration =
                beforeGeneration.plusSeconds(
                        REFRESH_TOKEN_EXPIRATION
                );

        long difference =
                Math.abs(
                        expiration.getEpochSecond()
                                - expectedExpiration.getEpochSecond()
                );

        assertTrue(
                difference <= 1,
                "Refresh token expiration should be approximately "
                        + REFRESH_TOKEN_EXPIRATION
                        + " seconds"
        );
    }

    private SignedJWT createSignedJwt(
            JWTClaimsSet claims
    ) throws JOSEException {

        SignedJWT jwt = new SignedJWT(
                new com.nimbusds.jose.JWSHeader(
                        com.nimbusds.jose.JWSAlgorithm.HS256
                ),
                claims
        );

        jwt.sign(
                new com.nimbusds.jose.crypto.MACSigner(
                        SECRET
                )
        );

        return jwt;
    }
}