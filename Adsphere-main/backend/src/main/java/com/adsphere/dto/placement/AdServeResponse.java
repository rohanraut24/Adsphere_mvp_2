package com.adsphere.dto.placement;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdServeResponse {
    private String title;
    private String description;
    private String imageUrl;
    private String destinationUrl;
}
