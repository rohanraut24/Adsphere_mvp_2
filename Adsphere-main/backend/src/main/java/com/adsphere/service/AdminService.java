package com.adsphere.service;

import com.adsphere.dto.admin.AdminStats;
import com.adsphere.dto.admin.UserResponse;
import com.adsphere.model.Role;
import com.adsphere.model.UserStatus;
import com.adsphere.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final BigDecimal NETWORK_RATE  = new BigDecimal("0.20");
    private static final BigDecimal PLATFORM_RATE = new BigDecimal("0.10");

    private final UserRepository userRepository;
    private final WebsiteRepository websiteRepository;
    private final CampaignRepository campaignRepository;
    private final AdPlacementRepository adPlacementRepository;
    private final AnalyticsRepository analyticsRepository;
    private final RevenueTransactionRepository revenueTransactionRepository;
    private final FraudDetectionService fraudDetectionService;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toUserResponse).toList();
    }

    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream().map(this::toUserResponse).toList();
    }

    public AdminStats getStats() {
        long totalUsers      = userRepository.count();
        long totalWebsites   = websiteRepository.count();
        long totalCampaigns  = campaignRepository.count();
        long totalPlacements = adPlacementRepository.count();

        var allAnalytics = analyticsRepository.findAll();
        long totalClicks      = allAnalytics.stream().mapToLong(a -> a.getClicks()).sum();
        long totalImpressions = allAnalytics.stream().mapToLong(a -> a.getImpressions()).sum();

        var allTx = revenueTransactionRepository.findAll();
        BigDecimal totalRevenue = allTx.stream()
                .map(t -> t.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal platformRevenue = totalRevenue.multiply(PLATFORM_RATE).setScale(4, RoundingMode.HALF_UP);
        BigDecimal networkRevenue  = totalRevenue.multiply(NETWORK_RATE).setScale(4, RoundingMode.HALF_UP);

        // --- Anomaly Detection ---
        java.util.List<com.adsphere.dto.admin.AnomalyReport> anomalies = new java.util.ArrayList<>();
        
        // 1. Suspicious CTR Detection (Click Fraud)
        var placementAnalytics = allAnalytics.stream()
                .collect(java.util.stream.Collectors.groupingBy(a -> a.getPlacement()));
        placementAnalytics.forEach((placement, analyticsList) -> {
            long pImpressions = analyticsList.stream().mapToLong(a -> a.getImpressions()).sum();
            long pClicks = analyticsList.stream().mapToLong(a -> a.getClicks()).sum();
            if (pImpressions > 50) {
                double ctr = (double) pClicks / pImpressions;
                if (ctr > 0.25) { // > 25% CTR is highly suspicious
                    anomalies.add(new com.adsphere.dto.admin.AnomalyReport(
                            "HIGH", "FRAUD_CTR",
                            String.format("Suspiciously high CTR (%.1f%%) detected on placement.", ctr * 100),
                            "PUBLISHER", placement.getWebsite().getPublisher().getId()
                    ));
                }
            }
            if (pImpressions > 1000 && pClicks == 0) {
                anomalies.add(new com.adsphere.dto.admin.AnomalyReport(
                        "LOW", "DEAD_INVENTORY",
                        "High impressions but 0 clicks. Ad rendering might be broken.",
                        "WEBSITE", placement.getWebsite().getId()
                ));
            }
        });

        // 2. Budget Exhaustion
        var campaigns = campaignRepository.findAll();
        for (var campaign : campaigns) {
            if (campaign.getStatus() == com.adsphere.model.CampaignStatus.ACTIVE && campaign.getBudget().compareTo(BigDecimal.ZERO) <= 0) {
                anomalies.add(new com.adsphere.dto.admin.AnomalyReport(
                        "MEDIUM", "BUDGET_EXHAUSTED",
                        "Campaign is active but budget is zero.",
                        "CAMPAIGN", campaign.getId()
                ));
            }
        }
        
        anomalies.addAll(fraudDetectionService.getRealtimeAnomalies());

        return new AdminStats(totalUsers, totalWebsites, totalCampaigns, totalPlacements,
                totalClicks, totalImpressions, platformRevenue, networkRevenue, anomalies);
    }

    public void suspendUser(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);
    }

    public void activateUser(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    private UserResponse toUserResponse(com.adsphere.model.User u) {
        UserResponse r = new UserResponse();
        r.setId(u.getId());
        r.setEmail(u.getEmail());
        r.setFullName(u.getFullName());
        r.setRole(u.getRole());
        r.setStatus(u.getStatus());
        r.setCreatedAt(u.getCreatedAt());
        return r;
    }
}
