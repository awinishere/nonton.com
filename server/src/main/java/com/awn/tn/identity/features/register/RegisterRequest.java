package com.awn.tn.identity.features.register;

import com.awn.tn.identity.domain.extensions.GenderType;

import java.time.LocalDate;

public record RegisterRequest(
        String name,
        String email,
        String password,
        GenderType gender,
        LocalDate birthday
) {
}
