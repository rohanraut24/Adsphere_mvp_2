package com.adsphere.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyReport {
    private String severity; // "HIGH", "MEDIUM", "LOW"
    private String type;     // "FRAUD_CTR", "DEAD_INVENTORY", "BUDGET_EXHAUSTED"
    private String description;
    private String entityType; // "PUBLISHER", "CAMPAIGN", "WEBSITE"
    private Long entityId;
}
