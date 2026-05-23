package com.fintek.auth.support;

import com.fintek.auth.dto.request.LoginRequest;
import com.fintek.auth.dto.request.RegisterRequest;
import com.fintek.auth.entity.UserAccount;
import com.fintek.auth.enums.AccountStatus;
import com.fintek.auth.enums.Role;
import java.time.Instant;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class TestDataFactory {
    public static final String EMAIL = "merchant@example.test";
    public static final String PASSWORD = "ValidPass123";

    private TestDataFactory() {
    }

    public static RegisterRequest registerRequest() {
        return new RegisterRequest("Ada Merchant", EMAIL, PASSWORD);
    }

    public static LoginRequest loginRequest() {
        return new LoginRequest(EMAIL, PASSWORD);
    }

    public static UserAccount activeUser() {
        UserAccount user = new UserAccount();
        user.setId("usr_1");
        user.setEmail(EMAIL);
        user.setFullName("Ada Merchant");
        user.setPasswordHash(new BCryptPasswordEncoder().encode(PASSWORD));
        user.setRole(Role.MERCHANT);
        user.setStatus(AccountStatus.ACTIVE);
        user.setCreatedAt(Instant.parse("2026-05-01T10:15:30Z"));
        user.setUpdatedAt(user.getCreatedAt());
        return user;
    }
}
