package com.fintek.auth.service;

import com.fintek.auth.dto.request.LoginRequest;
import com.fintek.auth.dto.request.RefreshTokenRequest;
import com.fintek.auth.entity.RefreshToken;
import com.fintek.auth.entity.UserAccount;
import com.fintek.auth.enums.AccountStatus;
import com.fintek.auth.exception.AuthException;
import com.fintek.auth.mapper.UserMapper;
import com.fintek.auth.repository.RefreshTokenRepository;
import com.fintek.auth.repository.UserAccountRepository;
import com.fintek.auth.service.impl.AuthServiceImpl;
import com.fintek.auth.support.TestDataFactory;
import com.fintek.auth.util.JwtService;
import com.fintek.auth.util.TokenHashes;
import com.fintek.auth.validator.RegistrationValidator;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    private static final String SECRET = "test-secret-with-enough-length-for-hmac-sha-signing-512-bits";

    @Mock
    private UserAccountRepository users;
    @Mock
    private RefreshTokenRepository refreshTokens;

    private PasswordEncoder passwordEncoder;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthServiceImpl(users, refreshTokens, passwordEncoder,
                new JwtService("payment-platform-test", SECRET, 15), new UserMapper(),
                new RegistrationValidator(users), 7);
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        when(users.existsByEmailIgnoreCase(TestDataFactory.EMAIL)).thenReturn(false);
        when(users.save(any(UserAccount.class))).thenAnswer(answer -> answer.getArgument(0));

        var response = authService.register(TestDataFactory.registerRequest());

        assertEquals(TestDataFactory.EMAIL, response.email(), "Registered email should be normalized");
        assertEquals(AccountStatus.ACTIVE, response.status(), "New users should be active by default");
        verify(users).save(any(UserAccount.class));
    }

    @Test
    void shouldRejectDuplicateEmailRegistration() {
        when(users.existsByEmailIgnoreCase(TestDataFactory.EMAIL)).thenReturn(true);

        AuthException error = assertThrows(AuthException.class,
                () -> authService.register(TestDataFactory.registerRequest()),
                "Duplicate emails should be rejected before saving");

        assertEquals(409, error.status(), "Duplicate registration should map to conflict status");
        verify(users, never()).save(any());
    }

    @Test
    void shouldEncryptPasswordUsingBCrypt() {
        when(users.save(any(UserAccount.class))).thenAnswer(answer -> answer.getArgument(0));

        authService.register(TestDataFactory.registerRequest());

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(users).save(captor.capture());
        assertNotEquals(TestDataFactory.PASSWORD, captor.getValue().getPasswordHash(),
                "Raw passwords must not be persisted");
        assertTrue(passwordEncoder.matches(TestDataFactory.PASSWORD, captor.getValue().getPasswordHash()),
                "Persisted password hash should be verifiable with BCrypt");
    }

    @Test
    void shouldLoginSuccessfullyWithValidCredentials() {
        UserAccount user = TestDataFactory.activeUser();
        when(users.findByEmailIgnoreCase(TestDataFactory.EMAIL)).thenReturn(Optional.of(user));
        when(users.save(user)).thenReturn(user);

        var response = authService.login(TestDataFactory.loginRequest());

        assertNotNull(response.accessToken(), "Successful login should issue an access token");
        assertNotNull(response.refreshToken(), "Successful login should issue a refresh token");
        assertEquals(0, user.getFailedLoginAttempts(), "Failed attempts should reset after successful login");
        verify(refreshTokens).save(any(RefreshToken.class));
    }

    @Test
    void shouldRejectLoginWithInvalidPassword() {
        UserAccount user = TestDataFactory.activeUser();
        when(users.findByEmailIgnoreCase(TestDataFactory.EMAIL)).thenReturn(Optional.of(user));

        AuthException error = assertThrows(AuthException.class,
                () -> authService.login(new LoginRequest(TestDataFactory.EMAIL, "wrongPassword")),
                "Invalid passwords should not authenticate");

        assertEquals(401, error.status(), "Invalid credentials should map to unauthorized");
        assertEquals(1, user.getFailedLoginAttempts(), "Failed attempt count should increase");
    }

    @Test
    void shouldRejectLoginForInactiveUser() {
        UserAccount user = TestDataFactory.activeUser();
        user.setStatus(AccountStatus.DISABLED);
        when(users.findByEmailIgnoreCase(TestDataFactory.EMAIL)).thenReturn(Optional.of(user));

        AuthException error = assertThrows(AuthException.class, () -> authService.login(TestDataFactory.loginRequest()),
                "Disabled users should not be allowed to log in");

        assertEquals(403, error.status(), "Inactive users should map to forbidden");
    }

    @Test
    void shouldLockAccountAfterMultipleFailedAttempts() {
        UserAccount user = TestDataFactory.activeUser();
        user.setFailedLoginAttempts(4);
        when(users.findByEmailIgnoreCase(TestDataFactory.EMAIL)).thenReturn(Optional.of(user));

        assertThrows(AuthException.class, () -> authService.login(new LoginRequest(TestDataFactory.EMAIL, "badPass")),
                "Fifth failed login should be rejected and lock the account");

        assertEquals(AccountStatus.LOCKED, user.getStatus(), "Account should be locked after five failures");
        assertNotNull(user.getLockedUntil(), "Lock timeout should be recorded");
    }

    @Test
    void shouldRefreshWithValidRefreshTokenAndRevokeOldToken() {
        UserAccount user = TestDataFactory.activeUser();
        RefreshToken refresh = new RefreshToken();
        refresh.setUser(user);
        refresh.setTokenHash(TokenHashes.sha256("raw-refresh"));
        refresh.setCreatedAt(Instant.now());
        refresh.setExpiresAt(Instant.now().plusSeconds(60));
        when(refreshTokens.findByTokenHashAndRevokedFalse(refresh.getTokenHash())).thenReturn(Optional.of(refresh));

        var response = authService.refresh(new RefreshTokenRequest("raw-refresh"));

        assertTrue(refresh.isRevoked(), "Used refresh token should be revoked");
        assertNotNull(response.accessToken(), "Refresh flow should issue a new access token");
        verify(refreshTokens, times(1)).save(any(RefreshToken.class));
    }
}
