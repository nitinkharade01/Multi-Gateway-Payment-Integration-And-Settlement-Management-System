package com.fintek.auth.service;

import com.fintek.auth.dto.request.LoginRequest;
import com.fintek.auth.dto.request.RefreshTokenRequest;
import com.fintek.auth.dto.request.RegisterRequest;
import com.fintek.auth.dto.response.AuthResponse;
import com.fintek.auth.dto.response.UserProfileResponse;

public interface AuthService {
    UserProfileResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(RefreshTokenRequest request);
    UserProfileResponse profile(String email);
}
