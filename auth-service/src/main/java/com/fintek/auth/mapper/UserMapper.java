package com.fintek.auth.mapper;

import com.fintek.auth.dto.response.UserProfileResponse;
import com.fintek.auth.entity.UserAccount;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserProfileResponse profile(UserAccount user) {
        return new UserProfileResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole(),
                user.getStatus(), user.getCreatedAt());
    }
}
