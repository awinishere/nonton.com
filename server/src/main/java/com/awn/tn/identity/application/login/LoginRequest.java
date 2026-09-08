package com.awn.tn.identity.application.login;

public record LoginRequest(
        String email,
        String password
) {
}
