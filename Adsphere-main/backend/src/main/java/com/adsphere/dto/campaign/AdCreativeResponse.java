package com.adsphere.dto.campaign;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdCreativeResponse {
    private Long id;
    private Long campaignId;
    private String title;
    private String description;
    private String imageUrl;
    private String destinationUrl;
    private LocalDateTime createdAt;
}
