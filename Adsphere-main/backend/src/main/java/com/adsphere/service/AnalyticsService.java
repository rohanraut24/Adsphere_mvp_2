package com.adsphere.service;

import com.adsphere.dto.analytics.*;
import com.adsphere.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final RevenueTransactionRepository revenueTransactionRepository;
    private final CampaignRepository campaignRepository;
    private final WebsiteRepository websiteRepository;
    private final UserRepository userRepository;

    public AnalyticsSummary getCampaignSummary(Long campaignId, LocalDate from, LocalDate to) {
        var rows = analyticsRepository.findByCampaignIdAndDateRange(campaignId, from, to);
        long impressions = rows.stream().mapToLong(a -> a.getImpressions()).sum();
        long clicks      = rows.stream().mapToLong(a -> a.getClicks()).sum();
        BigDecimal revenue = revenueTransactionRepository
                .sumAdvertiserSpend(getCampaignAdvertiserId(campaignId));
        return new AnalyticsSummary(impressions, clicks, calcCtr(impressions, clicks),
                revenue != null ? revenue : BigDecimal.ZERO);
    }

    public List<DailyAnalytics> getCampaignDaily(Long campaignId, LocalDate from, LocalDate to) {
        return analyticsRepository.findByCampaignIdAndDateRange(campaignId, from, to)
                .stream()
                .map(a -> new DailyAnalytics(a.getDate(), a.getImpressions(), a.getClicks(), BigDecimal.ZERO))
                .toList();
    }

    public AnalyticsSummary getWebsiteSummary(Long websiteId, LocalDate from, LocalDate to) {
        var rows = analyticsRepository.findByWebsiteIdAndDateRange(websiteId, from, to);
        long impressions = rows.stream().mapToLong(a -> a.getImpressions()).sum();
        long clicks      = rows.stream().mapToLong(a -> a.getClicks()).sum();
        BigDecimal revenue = revenueTransactionRepository
                .sumPublisherEarnings(getWebsitePublisherId(websiteId));
        return new AnalyticsSummary(impressions, clicks, calcCtr(impressions, clicks),
                revenue != null ? revenue : BigDecimal.ZERO);
    }

    public List<DailyAnalytics> getWebsiteDaily(Long websiteId, LocalDate from, LocalDate to) {
        return analyticsRepository.findByWebsiteIdAndDateRange(websiteId, from, to)
                .stream()
                .map(a -> new DailyAnalytics(a.getDate(), a.getImpressions(), a.getClicks(), BigDecimal.ZERO))
                .toList();
    }

    public List<DailyAnalytics> getGlobalAdvertiserDaily(Long advertiserId, LocalDate from, LocalDate to) {
        var transactions = revenueTransactionRepository.findByAdvertiserAndDateRange(
                advertiserId, from.atStartOfDay(), to.plusDays(1).atStartOfDay());
        
        java.util.Map<LocalDate, BigDecimal> dailySpend = new java.util.HashMap<>();
        for (var t : transactions) {
            LocalDate d = t.getCreatedAt().toLocalDate();
            dailySpend.put(d, dailySpend.getOrDefault(d, BigDecimal.ZERO).add(t.getTotalAmount()));
        }
        
        return from.datesUntil(to.plusDays(1)).map(d -> 
            new DailyAnalytics(d, 0, 0, dailySpend.getOrDefault(d, BigDecimal.ZERO))
        ).toList();
    }

    public List<DailyAnalytics> getGlobalPublisherDaily(Long publisherId, LocalDate from, LocalDate to) {
        var transactions = revenueTransactionRepository.findByPublisherAndDateRange(
                publisherId, from.atStartOfDay(), to.plusDays(1).atStartOfDay());
        
        java.util.Map<LocalDate, BigDecimal> dailyEarnings = new java.util.HashMap<>();
        for (var t : transactions) {
            LocalDate d = t.getCreatedAt().toLocalDate();
            dailyEarnings.put(d, dailyEarnings.getOrDefault(d, BigDecimal.ZERO).add(t.getPublisherShare()));
        }
        
        return from.datesUntil(to.plusDays(1)).map(d -> 
            new DailyAnalytics(d, 0, 0, dailyEarnings.getOrDefault(d, BigDecimal.ZERO))
        ).toList();
    }

    private BigDecimal calcCtr(long impressions, long clicks) {
        if (impressions == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(clicks)
                .divide(BigDecimal.valueOf(impressions), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private Long getCampaignAdvertiserId(Long campaignId) {
        return campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"))
                .getAdvertiser().getId();
    }

    private Long getWebsitePublisherId(Long websiteId) {
        return websiteRepository.findById(websiteId)
                .orElseThrow(() -> new IllegalArgumentException("Website not found"))
                .getPublisher().getId();
    }
}
