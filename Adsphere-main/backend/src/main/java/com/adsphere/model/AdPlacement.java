package com.adsphere.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "ad_placements")
@Data
public class AdPlacement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "website_id", nullable = false)
    private Website website;

    @ManyToOne
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne
    @JoinColumn(name = "ad_creative_id")
    private AdCreative adCreative;

    private boolean active = true;
    private LocalDateTime createdAt = LocalDateTime.now();
}
