package com.fintek.auth.service.impl;

import com.fintek.auth.dto.request.LoginRequest;
import com.fintek.auth.dto.request.RefreshTokenRequest;
import com.fintek.auth.dto.request.RegisterRequest;
import com.fintek.auth.dto.response.AuthResponse;
import com.fintek.auth.dto.response.UserProfileResponse;
import com.fintek.auth.entity.RefreshToken;
import com.fintek.auth.entity.UserAccount;
import com.fintek.auth.enums.AccountStatus;
import com.fintek.auth.enums.Role;
import com.fintek.auth.exception.AuthException;
import com.fintek.auth.mapper.UserMapper;
import com.fintek.auth.repository.RefreshTokenRepository;
import com.fintek.auth.repository.UserAccountRepository;
import com.fintek.auth.service.AuthService;
import com.fintek.auth.util.JwtService;
import com.fintek.auth.util.TokenHashes;
import com.fintek.auth.validator.RegistrationValidator;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final int MAX_FAILURES = 5;
    private final UserAccountRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwords;
    private final JwtService jwtService;
    private final UserMapper mapper;
    private final RegistrationValidator registrationValidator;
    private final long refreshTtlDays;

    public AuthServiceImpl(UserAccountRepository users, RefreshTokenRepository refreshTokens, PasswordEncoder passwords,
                           JwtService jwtService, UserMapper mapper, RegistrationValidator registrationValidator,
                           @Value("${security.jwt.refresh-ttl-days}") long refreshTtlDays) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.passwords = passwords;
        this.jwtService = jwtService;
        this.mapper = mapper;
        this.registrationValidator = registrationValidator;
        this.refreshTtlDays = refreshTtlDays;
    }

    @Override
    @Transactional
    public UserProfileResponse register(RegisterRequest request) {
        registrationValidator.validate(request);
        Instant now = Instant.now();
        UserAccount user = new UserAccount();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        user.setFullName(request.fullName().trim());
        user.setPasswordHash(passwords.encode(request.password()));
        user.setRole(Role.MERCHANT);
        user.setStatus(AccountStatus.ACTIVE);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        log.info("Registered user {} as {}", user.getEmail(), user.getRole());
        return mapper.profile(users.save(user));
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserAccount user = users.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new AuthException(401, "Invalid credentials"));
        Instant now = Instant.now();
        releaseExpiredLock(user, now);
        ensureActive(user, now);
        if (!passwords.matches(request.password(), user.getPasswordHash())) {
            recordFailure(user, now);
            throw new AuthException(401, "Invalid credentials");
        }
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setUpdatedAt(now);
        users.save(user);
        log.info("User {} authenticated", user.getEmail());
        return issueSession(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String hash = TokenHashes.sha256(request.refreshToken());
        RefreshToken refresh = refreshTokens.findByTokenHashAndRevokedFalse(hash)
                .orElseThrow(() -> new AuthException(401, "Refresh token is invalid"));
        if (!refresh.getExpiresAt().isAfter(Instant.now())) {
            refresh.setRevoked(true);
            throw new AuthException(401, "Refresh token expired");
        }
        refresh.setRevoked(true);
        ensureActive(refresh.getUser(), Instant.now());
        return issueSession(refresh.getUser());
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse profile(String email) {
        return users.findByEmailIgnoreCase(email)
                .map(mapper::profile)
                .orElseThrow(() -> new AuthException(404, "User profile not found"));
    }

    private AuthResponse issueSession(UserAccount user) {
        JwtService.IssuedToken access = jwtService.issue(user);
        String rawRefresh = UUID.randomUUID() + "." + UUID.randomUUID();
        RefreshToken refresh = new RefreshToken();
        refresh.setId(UUID.randomUUID().toString());
        refresh.setUser(user);
        refresh.setTokenHash(TokenHashes.sha256(rawRefresh));
        refresh.setCreatedAt(Instant.now());
        refresh.setExpiresAt(Instant.now().plus(refreshTtlDays, ChronoUnit.DAYS));
        refreshTokens.save(refresh);
        return new AuthResponse(access.value(), rawRefresh, access.expiresAt(), mapper.profile(user));
    }

    private void recordFailure(UserAccount user, Instant now) {
        int failures = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(failures);
        user.setUpdatedAt(now);
        if (failures >= MAX_FAILURES) {
            user.setStatus(AccountStatus.LOCKED);
            user.setLockedUntil(now.plus(15, ChronoUnit.MINUTES));
            log.warn("Locked user {} after {} failed logins", user.getEmail(), failures);
        }
        users.save(user);
    }

    private void releaseExpiredLock(UserAccount user, Instant now) {
        if (user.lockExpired(now)) {
            user.setStatus(AccountStatus.ACTIVE);
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        }
    }

    private void ensureActive(UserAccount user, Instant now) {
        if (user.getStatus() == AccountStatus.LOCKED && user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            throw new AuthException(423, "Account is locked until " + user.getLockedUntil());
        }
        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new AuthException(403, "Account is not active");
        }
    }
}
