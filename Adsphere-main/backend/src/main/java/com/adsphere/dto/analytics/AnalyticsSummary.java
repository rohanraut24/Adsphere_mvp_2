package com.adsphere.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AnalyticsSummary {
    private long totalImpressions;
    private long totalClicks;
    private BigDecimal ctr; // click-through rate %
    private BigDecimal totalRevenue;
}
