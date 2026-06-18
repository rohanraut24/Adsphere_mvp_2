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

    public RevenueResponse recordClick(Long placementId) {
        AdPlacement placement = placementService.getPlacementEntity(placementId);

        if (!placement.isActive())
            throw new IllegalStateException("Placement is not active");

        Campaign campaign = placement.getCampaign();
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
        tx.setPublisher(placement.getWebsite().getPublisher());
        tx.setAdvertiser(campaign.getAdvertiser());
        tx.setTotalAmount(cpc);
        tx.setPublisherShare(cpc.multiply(PUBLISHER_RATE).setScale(4, RoundingMode.HALF_UP));
        tx.setNetworkShare(cpc.multiply(NETWORK_RATE).setScale(4, RoundingMode.HALF_UP));
        tx.setPlatformShare(cpc.multiply(PLATFORM_RATE).setScale(4, RoundingMode.HALF_UP));

        revenueTransactionRepository.save(tx);
        updateAnalytics(placement, false, true);

        return toResponse(tx);
    }

    public void recordImpression(Long placementId) {
        AdPlacement placement = placementService.getPlacementEntity(placementId);
        if (placement.isActive()) updateAnalytics(placement, true, false);
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

    private void updateAnalytics(AdPlacement placement, boolean impression, boolean click) {
        LocalDate today = LocalDate.now();
        Optional<Analytics> existing = analyticsRepository.findByPlacementAndDate(placement, today);

        Analytics analytics = existing.orElseGet(() -> {
            Analytics a = new Analytics();
            a.setPlacement(placement);
            a.setDate(today);
            return a;
        });

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
