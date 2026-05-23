package com.fintek.auth.validator;

import com.fintek.auth.dto.request.RegisterRequest;
import com.fintek.auth.exception.AuthException;
import com.fintek.auth.repository.UserAccountRepository;
import org.springframework.stereotype.Component;

@Component
public class RegistrationValidator {
    private final UserAccountRepository users;

    public RegistrationValidator(UserAccountRepository users) {
        this.users = users;
    }

    public void validate(RegisterRequest request) {
        if (users.existsByEmailIgnoreCase(request.email().trim())) {
            throw new AuthException(409, "An account already exists for this email");
        }
        if (request.password().toLowerCase().contains(request.email().split("@")[0].toLowerCase())) {
            throw new AuthException(400, "Password must not contain the email handle");
        }
    }
}
