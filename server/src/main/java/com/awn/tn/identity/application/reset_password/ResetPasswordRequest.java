package com.awn.tn.identity.application.reset_password;

public record ResetPasswordRequest(
        String currentPassword,
        String password
) {
}
