package com.adsphere.dto.campaign;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CampaignRequest {
    @NotBlank
    private String name;
    private String description;
    @NotNull @DecimalMin("1.00")
    private BigDecimal budget;
    @NotNull @DecimalMin("0.01")
    private BigDecimal cpcBid;
    private LocalDate startDate;
    private LocalDate endDate;
}
