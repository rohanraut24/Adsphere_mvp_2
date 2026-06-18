package com.adsphere.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "ad_creatives")
@Data
public class AdCreative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(nullable = false)
    private String title;

    private String description;
    private String imageUrl;
    private String destinationUrl;

    private LocalDateTime createdAt = LocalDateTime.now();
}
