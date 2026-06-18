package com.adsphere.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "analytics")
@Data
public class Analytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "placement_id", nullable = false)
    private AdPlacement placement;

    @Column(nullable = false)
    private LocalDate date;

    private long impressions = 0;
    private long clicks = 0;
}
