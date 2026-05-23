package com.fintek.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintek.auth.config.SecurityConfig;
import com.fintek.auth.dto.request.LoginRequest;
import com.fintek.auth.dto.response.AuthResponse;
import com.fintek.auth.dto.response.UserProfileResponse;
import com.fintek.auth.enums.AccountStatus;
import com.fintek.auth.enums.Role;
import com.fintek.auth.exception.AuthException;
import com.fintek.auth.service.AuthService;
import com.fintek.auth.support.MockJwtFactory;
import com.fintek.auth.support.TestDataFactory;
import com.fintek.auth.util.JwtService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private AuthService authService;
    @MockBean
    private JwtService jwtService;

    @Test
    void shouldAllowPublicAccessToLoginAndRegister() throws Exception {
        when(authService.register(any())).thenReturn(profile());
        when(authService.login(any())).thenReturn(new AuthResponse("jwt", "refresh", Instant.now().plusSeconds(60), profile()));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.registerRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(TestDataFactory.EMAIL));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.loginRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt"));
    }

    @Test
    void shouldReturnBadRequestForInvalidEmailAndBlankPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Ada Merchant","email":"not-email","password":""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenThrow(new AuthException(401, "Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.loginRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldBlockProfileWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/auth/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowProfileWithValidJwt() throws Exception {
        when(jwtService.parse("valid-token")).thenReturn(MockJwtFactory.merchantClaims(TestDataFactory.EMAIL));
        when(authService.profile(TestDataFactory.EMAIL)).thenReturn(profile());

        mockMvc.perform(get("/api/auth/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(TestDataFactory.EMAIL));
    }

    private UserProfileResponse profile() {
        return new UserProfileResponse("usr_1", "Ada Merchant", TestDataFactory.EMAIL, Role.MERCHANT,
                AccountStatus.ACTIVE, Instant.parse("2026-05-01T10:15:30Z"));
    }
}
