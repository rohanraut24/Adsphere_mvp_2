package com.adsphere.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DailyAnalytics {
    private LocalDate date;
    private long impressions;
    private long clicks;
    private BigDecimal revenue;
}
