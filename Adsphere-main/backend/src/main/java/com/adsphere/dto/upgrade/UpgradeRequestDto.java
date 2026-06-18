package com.adsphere.dto.upgrade;

import com.adsphere.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpgradeRequestDto {
    @NotNull
    private Role requestedRole;
    private String reason;
}
