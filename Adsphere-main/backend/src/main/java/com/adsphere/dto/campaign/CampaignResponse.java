package com.adsphere.dto.campaign;

import com.adsphere.model.CampaignStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CampaignResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal budget;
    private BigDecimal cpcBid;
    private LocalDate startDate;
    private LocalDate endDate;
    private CampaignStatus status;
    private String advertiserEmail;
    private LocalDateTime createdAt;
    private java.util.List<AdCreativeResponse> creatives;
}
