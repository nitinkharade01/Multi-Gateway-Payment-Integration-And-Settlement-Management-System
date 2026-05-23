package com.fintek.auth.controller;

import com.fintek.auth.dto.request.LoginRequest;
import com.fintek.auth.dto.request.RefreshTokenRequest;
import com.fintek.auth.dto.request.RegisterRequest;
import com.fintek.auth.dto.response.AuthResponse;
import com.fintek.auth.dto.response.UserProfileResponse;
import com.fintek.auth.service.AuthService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    UserProfileResponse register(@Valid @RequestBody RegisterRequest request) {
        return auth.register(request);
    }

    @PostMapping("/login")
    AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return auth.login(request);
    }

    @PostMapping("/refresh")
    AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return auth.refresh(request);
    }

    @GetMapping("/profile")
    UserProfileResponse profile(Principal principal) {
        return auth.profile(principal.getName());
    }
}
