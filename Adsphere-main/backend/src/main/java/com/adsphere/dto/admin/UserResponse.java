package com.adsphere.dto.admin;

import com.adsphere.model.Role;
import com.adsphere.model.UserStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private Role role;
    private UserStatus status;
    private LocalDateTime createdAt;
}
