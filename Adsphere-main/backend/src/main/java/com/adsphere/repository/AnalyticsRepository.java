package com.adsphere.repository;

import com.adsphere.model.AdPlacement;
import com.adsphere.model.Analytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AnalyticsRepository extends JpaRepository<Analytics, Long> {
    List<Analytics> findByPlacement(AdPlacement placement);
    Optional<Analytics> findByPlacementAndDate(AdPlacement placement, LocalDate date);

    @Query("SELECT a FROM Analytics a WHERE a.placement.campaign.id = :campaignId AND a.date BETWEEN :from AND :to")
    List<Analytics> findByCampaignIdAndDateRange(@Param("campaignId") Long campaignId,
                                                  @Param("from") LocalDate from,
                                                  @Param("to") LocalDate to);

    @Query("SELECT a FROM Analytics a WHERE a.placement.website.id = :websiteId AND a.date BETWEEN :from AND :to")
    List<Analytics> findByWebsiteIdAndDateRange(@Param("websiteId") Long websiteId,
                                                 @Param("from") LocalDate from,
                                                 @Param("to") LocalDate to);
}
