package com.adsphere.service;

import com.adsphere.dto.placement.RevenueResponse;
import com.adsphere.model.*;
import com.adsphere.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RevenueService {

    private static final BigDecimal PUBLISHER_RATE = new BigDecimal("0.70");
    private static final BigDecimal NETWORK_RATE   = new BigDecimal("0.20");
    private static final BigDecimal PLATFORM_RATE  = new BigDecimal("0.10");

    private final RevenueTransactionRepository revenueTransactionRepository;
    private final AnalyticsRepository analyticsRepository;
    private final CampaignRepository campaignRepository;
    private final PlacementService placementService;
    private final UserRepository userRepository;

    public RevenueResponse recordClick(Long placementId, Long campaignId) {
        AdPlacement placement = placementService.getPlacementEntity(placementId);

        if (!placement.isActive())
            throw new IllegalStateException("Placement is not active");

        Campaign campaign = placement.getCampaign();
        if (campaign == null) {
            if (campaignId == null) {
                throw new IllegalArgumentException("Campaign ID is required for dynamic placement clicks");
            }
            campaign = campaignRepository.findById(campaignId)
                    .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        } else {
            if (campaignId != null && !campaign.getId().equals(campaignId)) {
                throw new IllegalArgumentException("Campaign ID mismatch for manual placement");
            }
        }

        if (campaign.getStatus() != CampaignStatus.ACTIVE)
            throw new IllegalStateException("Campaign is not active");

        BigDecimal cpc = campaign.getCpcBid();

        // Gap 3 fix: deduct from budget, auto-pause if exhausted
        BigDecimal newBudget = campaign.getBudget().subtract(cpc);
        if (newBudget.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalStateException("Campaign budget exhausted");
        campaign.setBudget(newBudget);
        if (newBudget.compareTo(BigDecimal.ZERO) == 0)
            campaign.setStatus(CampaignStatus.COMPLETED);
        campaignRepository.save(campaign);

        RevenueTransaction tx = new RevenueTransaction();
        tx.setPlacement(placement);
        tx.setCampaign(campaign);
        tx.setPublisher(placement.getWebsite().getPublisher());
        tx.setAdvertiser(campaign.getAdvertiser());
        tx.setTotalAmount(cpc);
        tx.setPublisherShare(cpc.multiply(PUBLISHER_RATE).setScale(4, RoundingMode.HALF_UP));
        tx.setNetworkShare(cpc.multiply(NETWORK_RATE).setScale(4, RoundingMode.HALF_UP));
        tx.setPlatformShare(cpc.multiply(PLATFORM_RATE).setScale(4, RoundingMode.HALF_UP));

        revenueTransactionRepository.save(tx);
        
        // Credit the publisher's wallet in real-time
        User publisher = placement.getWebsite().getPublisher();
        publisher.setBalance(publisher.getBalance().add(tx.getPublisherShare()));
        userRepository.save(publisher);

        updateAnalytics(placement, campaign, false, true);

        return toResponse(tx);
    }

    public void recordImpression(Long placementId) {
        recordImpression(placementId, null);
    }

    public void recordImpression(Long placementId, Long campaignId) {
        AdPlacement placement = placementService.getPlacementEntity(placementId);
        if (placement.isActive()) {
            Campaign campaign = null;
            if (campaignId != null) {
                campaign = campaignRepository.findById(campaignId).orElse(null);
            }
            updateAnalytics(placement, campaign, true, false);
        }
    }

    public BigDecimal getPublisherEarnings(Long publisherId) {
        BigDecimal result = revenueTransactionRepository.sumPublisherEarnings(publisherId);
        return result != null ? result : BigDecimal.ZERO;
    }

    public BigDecimal getAdvertiserSpend(Long advertiserId) {
        BigDecimal result = revenueTransactionRepository.sumAdvertiserSpend(advertiserId);
        return result != null ? result : BigDecimal.ZERO;
    }

    public List<RevenueResponse> getTransactionsByPublisher(User publisher) {
        return revenueTransactionRepository.findByPublisher(publisher)
                .stream().map(this::toResponse).toList();
    }

    public List<RevenueResponse> getTransactionsByAdvertiser(User advertiser) {
        return revenueTransactionRepository.findByAdvertiser(advertiser)
                .stream().map(this::toResponse).toList();
    }

    private synchronized void updateAnalytics(AdPlacement placement, Campaign campaign, boolean impression, boolean click) {
        LocalDate today = LocalDate.now();
        List<Analytics> existing = analyticsRepository.findByPlacementAndCampaignAndDate(placement, campaign, today);

        Analytics analytics;
        if (existing.isEmpty()) {
            analytics = new Analytics();
            analytics.setPlacement(placement);
            analytics.setCampaign(campaign);
            analytics.setDate(today);
        } else {
            analytics = existing.get(0);
            if (existing.size() > 1) {
                // If there are duplicate records due to concurrency, merge them and delete extra rows
                for (int i = 1; i < existing.size(); i++) {
                    Analytics extra = existing.get(i);
                    analytics.setImpressions(analytics.getImpressions() + extra.getImpressions());
                    analytics.setClicks(analytics.getClicks() + extra.getClicks());
                    analyticsRepository.delete(extra);
                }
            }
        }

        if (impression) analytics.setImpressions(analytics.getImpressions() + 1);
        if (click)      analytics.setClicks(analytics.getClicks() + 1);

        analyticsRepository.save(analytics);
    }

    private RevenueResponse toResponse(RevenueTransaction t) {
        RevenueResponse r = new RevenueResponse();
        r.setId(t.getId());
        r.setPlacementId(t.getPlacement().getId());
        r.setTotalAmount(t.getTotalAmount());
        r.setPublisherShare(t.getPublisherShare());
        r.setNetworkShare(t.getNetworkShare());
        r.setPlatformShare(t.getPlatformShare());
        r.setCreatedAt(t.getCreatedAt());
        return r;
    }
}
