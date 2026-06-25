package com.adsphere.dto.placement;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlacementRequest {
    @NotNull
    private Long websiteId;
    private Long campaignId;
    private Long adCreativeId;
}
