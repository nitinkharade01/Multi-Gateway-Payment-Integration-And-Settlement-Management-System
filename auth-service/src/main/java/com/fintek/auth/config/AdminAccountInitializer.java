package com.fintek.auth.config;

import com.fintek.auth.entity.UserAccount;
import com.fintek.auth.enums.AccountStatus;
import com.fintek.auth.enums.Role;
import com.fintek.auth.repository.UserAccountRepository;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminAccountInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final UserAccountRepository users;
    private final PasswordEncoder passwords;
    private final String defaultEmail;
    private final String defaultPassword;

    public AdminAccountInitializer(UserAccountRepository users,
                                   PasswordEncoder passwords,
                                   @Value("${app.admin.default-email:admin@payment.com}") String defaultEmail,
                                   @Value("${app.admin.default-password:}") String defaultPassword) {
        this.users = users;
        this.passwords = passwords;
        this.defaultEmail = defaultEmail;
        this.defaultPassword = defaultPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (defaultPassword == null || defaultPassword.isBlank()) {
            log.info("ADMIN_DEFAULT_PASSWORD is not set; skipping default admin bootstrap");
            return;
        }

        String email = defaultEmail.trim().toLowerCase(Locale.ROOT);
        if (users.findByEmailIgnoreCase(email).isPresent()) {
            log.info("Default admin {} already exists; leaving credentials unchanged", email);
            return;
        }

        Instant now = Instant.now();
        UserAccount admin = new UserAccount();
        admin.setId(UUID.randomUUID().toString());
        admin.setEmail(email);
        admin.setFullName("Platform Admin");
        admin.setPasswordHash(passwords.encode(defaultPassword));
        admin.setRole(Role.ADMIN);
        admin.setStatus(AccountStatus.ACTIVE);
        admin.setCreatedAt(now);
        admin.setUpdatedAt(now);
        users.save(admin);
        log.info("Bootstrapped default admin account {}", email);
    }
}
