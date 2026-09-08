package com.awn.tn.identity.application.register;

import java.util.UUID;

public record RegisterResponse(
        UUID userId,
        String name,
        String email
) {
}
