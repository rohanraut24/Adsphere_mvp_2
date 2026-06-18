package com.adsphere.dto.placement;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RevenueResponse {
    private Long id;
    private Long placementId;
    private BigDecimal totalAmount;
    private BigDecimal publisherShare;
    private BigDecimal networkShare;
    private BigDecimal platformShare;
    private LocalDateTime createdAt;
}
