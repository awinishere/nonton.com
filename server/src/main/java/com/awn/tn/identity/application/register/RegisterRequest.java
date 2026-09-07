package com.awn.tn.identity.application.register;

import com.awn.tn.identity.domain.extensions.GenderType;

import java.time.LocalDate;

public record RegisterRequest(
        String name,
        String email,
        String password

) {
}
