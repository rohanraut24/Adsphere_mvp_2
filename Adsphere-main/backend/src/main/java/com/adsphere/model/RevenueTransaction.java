package com.adsphere.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "revenue_transactions")
@Data
public class RevenueTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "placement_id", nullable = false)
    private AdPlacement placement;

    @ManyToOne
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @ManyToOne
    @JoinColumn(name = "publisher_id", nullable = false)
    private User publisher;

    @ManyToOne
    @JoinColumn(name = "advertiser_id", nullable = false)
    private User advertiser;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal publisherShare;   // 70%

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal networkShare;     // 20%

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal platformShare;    // 10%

    private LocalDateTime createdAt = LocalDateTime.now();
}
