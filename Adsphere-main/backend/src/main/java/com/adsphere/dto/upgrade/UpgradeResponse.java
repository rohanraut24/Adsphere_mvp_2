package com.adsphere.dto.upgrade;

import com.adsphere.model.Role;
import com.adsphere.model.UpgradeStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UpgradeResponse {
    private Long id;
    private String userEmail;
    private Role requestedRole;
    private UpgradeStatus status;
    private String reason;
    private String reviewNote;
    private String reviewedByEmail;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}
