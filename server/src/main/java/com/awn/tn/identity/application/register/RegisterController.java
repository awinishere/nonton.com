package com.awn.tn.identity.application.register;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1/auth/register")
@RequiredArgsConstructor
public class RegisterController {

    private final RegisterService service;

    @PostMapping
    public ResponseEntity<RegisterResponse> register(
            @RequestBody RegisterRequest request
    ){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.register(request));
    }
}
