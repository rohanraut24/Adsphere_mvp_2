package com.adsphere.dto.upgrade;

import com.adsphere.model.UpgradeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotNull
    private UpgradeStatus decision; // APPROVED or REJECTED
    private String reviewNote;
}
