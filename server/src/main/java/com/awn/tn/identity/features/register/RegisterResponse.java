package com.awn.tn.identity.features.register;

import com.awn.tn.identity.domain.extensions.GenderType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RegisterResponse(
        String name,
        String message
) {
}
