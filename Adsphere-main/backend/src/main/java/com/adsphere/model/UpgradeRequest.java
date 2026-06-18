package com.adsphere.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "upgrade_requests")
@Data
public class UpgradeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role requestedRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UpgradeStatus status = UpgradeStatus.PENDING;

    private String reason;
    private String reviewNote;

    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime reviewedAt;
}
