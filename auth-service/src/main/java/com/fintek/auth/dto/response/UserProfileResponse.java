package com.fintek.auth.dto.response;

import com.fintek.auth.enums.AccountStatus;
import com.fintek.auth.enums.Role;
import java.time.Instant;

public record UserProfileResponse(String userId, String fullName, String email, Role role, AccountStatus status,
                                  Instant createdAt) {
}
