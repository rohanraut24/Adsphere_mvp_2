package com.adsphere.dto.website;

import com.adsphere.model.WebsiteStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WebsiteResponse {
    private Long id;
    private String url;
    private String name;
    private String category;
    private WebsiteStatus status;
    private String publisherEmail;
    private LocalDateTime createdAt;
}
