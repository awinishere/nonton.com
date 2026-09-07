package com.awn.tn.identity.application.login;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
