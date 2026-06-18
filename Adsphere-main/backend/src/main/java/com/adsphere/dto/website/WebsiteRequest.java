package com.adsphere.dto.website;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WebsiteRequest {
    @NotBlank
    private String url;
    @NotBlank
    private String name;
    private String category;
}
