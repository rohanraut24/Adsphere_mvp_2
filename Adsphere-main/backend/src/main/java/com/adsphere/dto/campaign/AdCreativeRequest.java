package com.adsphere.dto.campaign;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdCreativeRequest {
    @NotBlank
    private String title;
    private String description;
    private String imageUrl;
    @NotBlank
    private String destinationUrl;
}
