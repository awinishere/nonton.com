package com.awn.tn.identity.application.reset_password;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class ResetPasswordController {

    private final ResetPasswordService resetPasswordService;

    @PutMapping("/password")
    public ResponseEntity<Void> resetPassword(
            @AuthenticationPrincipal UUID userId,
            @RequestBody ResetPasswordRequest request
    ) {
        resetPasswordService.resetPassword(
                userId,
                request
        );

        return ResponseEntity.noContent().build();
    }
}