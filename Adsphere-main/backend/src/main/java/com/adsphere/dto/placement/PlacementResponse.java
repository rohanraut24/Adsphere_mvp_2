package com.adsphere.dto.placement;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PlacementResponse {
    private Long id;
    private Long websiteId;
    private String websiteUrl;
    private Long campaignId;
    private String campaignName;
    private Long adCreativeId;
    private boolean active;
    private LocalDateTime createdAt;
}
