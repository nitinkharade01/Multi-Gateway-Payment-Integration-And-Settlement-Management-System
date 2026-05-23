package com.fintek.auth.repository;

import com.fintek.auth.entity.UserAccount;
import com.fintek.auth.enums.AccountStatus;
import com.fintek.auth.enums.Role;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserAccountRepositoryTest {
    @Autowired
    private UserAccountRepository users;

    @Test
    void shouldFindUserByEmail() {
        users.save(user("merchant@example.test"));

        var found = users.findByEmailIgnoreCase("MERCHANT@example.test");

        assertTrue(found.isPresent(), "Repository should find users case-insensitively by email");
        assertEquals("merchant@example.test", found.get().getEmail(), "Stored email should be returned");
    }

    @Test
    void shouldReturnEmptyForUnknownEmailAndCheckEmailExists() {
        users.save(user("merchant@example.test"));

        assertTrue(users.existsByEmailIgnoreCase("MERCHANT@example.test"), "Existing email should be detected");
        assertTrue(users.findByEmailIgnoreCase("unknown@example.test").isEmpty(),
                "Unknown email should return Optional.empty");
    }

    private UserAccount user(String email) {
        UserAccount user = new UserAccount();
        user.setId("usr_" + email.hashCode());
        user.setEmail(email);
        user.setFullName("Ada Merchant");
        user.setPasswordHash("$2a$10$012345678901234567890u3c7WZ6bUv9sQFzUj7zqE2B7tNqO7M7G");
        user.setRole(Role.MERCHANT);
        user.setStatus(AccountStatus.ACTIVE);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(user.getCreatedAt());
        return user;
    }
}
