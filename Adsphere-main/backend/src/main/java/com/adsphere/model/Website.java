package com.adsphere.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "websites")
@Data
public class Website {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "publisher_id", nullable = false)
    private User publisher;

    @Column(nullable = false, unique = true)
    private String url;

    private String name;
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WebsiteStatus status = WebsiteStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "website", cascade = CascadeType.ALL)
    private List<AdPlacement> adPlacements;
}
