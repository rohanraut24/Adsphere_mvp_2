package com.adsphere.repository;

import com.adsphere.model.RevenueTransaction;
import com.adsphere.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface RevenueTransactionRepository extends JpaRepository<RevenueTransaction, Long> {
    List<RevenueTransaction> findByPublisher(User publisher);
    List<RevenueTransaction> findByAdvertiser(User advertiser);

    @Query("SELECT SUM(r.publisherShare) FROM RevenueTransaction r WHERE r.publisher.id = :publisherId")
    BigDecimal sumPublisherEarnings(@Param("publisherId") Long publisherId);

    @Query("SELECT SUM(r.totalAmount) FROM RevenueTransaction r WHERE r.advertiser.id = :advertiserId")
    BigDecimal sumAdvertiserSpend(@Param("advertiserId") Long advertiserId);

    @Query("SELECT SUM(r.totalAmount) FROM RevenueTransaction r WHERE r.campaign.id = :campaignId")
    BigDecimal sumCampaignSpend(@Param("campaignId") Long campaignId);

    @Query("SELECT SUM(r.publisherShare) FROM RevenueTransaction r WHERE r.placement.website.id = :websiteId")
    BigDecimal sumWebsiteEarnings(@Param("websiteId") Long websiteId);

    @Query("SELECT r FROM RevenueTransaction r WHERE r.publisher.id = :publisherId AND r.createdAt >= :from AND r.createdAt <= :to")
    List<RevenueTransaction> findByPublisherAndDateRange(@Param("publisherId") Long publisherId, @Param("from") java.time.LocalDateTime from, @Param("to") java.time.LocalDateTime to);

    @Query("SELECT r FROM RevenueTransaction r WHERE r.advertiser.id = :advertiserId AND r.createdAt >= :from AND r.createdAt <= :to")
    List<RevenueTransaction> findByAdvertiserAndDateRange(@Param("advertiserId") Long advertiserId, @Param("from") java.time.LocalDateTime from, @Param("to") java.time.LocalDateTime to);

    @Query("SELECT r FROM RevenueTransaction r WHERE r.campaign.id = :campaignId AND r.createdAt >= :from AND r.createdAt <= :to")
    List<RevenueTransaction> findByCampaignAndDateRange(@Param("campaignId") Long campaignId, @Param("from") java.time.LocalDateTime from, @Param("to") java.time.LocalDateTime to);

    @Query("SELECT r FROM RevenueTransaction r WHERE r.placement.website.id = :websiteId AND r.createdAt >= :from AND r.createdAt <= :to")
    List<RevenueTransaction> findByWebsiteAndDateRange(@Param("websiteId") Long websiteId, @Param("from") java.time.LocalDateTime from, @Param("to") java.time.LocalDateTime to);
}
