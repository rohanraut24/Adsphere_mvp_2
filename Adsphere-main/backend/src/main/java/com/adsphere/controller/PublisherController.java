package com.adsphere.controller;

import com.adsphere.dto.analytics.*;
import com.adsphere.dto.placement.*;
import com.adsphere.dto.upgrade.*;
import com.adsphere.dto.website.*;
import com.adsphere.repository.UserRepository;
import com.adsphere.service.AnalyticsService;
import com.adsphere.service.PlacementService;
import com.adsphere.service.RevenueService;
import com.adsphere.service.UpgradeService;
import com.adsphere.service.WebsiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/publisher")
@RequiredArgsConstructor
public class PublisherController {

    private final WebsiteService websiteService;
    private final UpgradeService upgradeService;
    private final PlacementService placementService;
    private final RevenueService revenueService;
    private final UserRepository userRepository;
    private final AnalyticsService analyticsService;
    private final com.adsphere.service.CampaignService campaignService;

    // --- Campaign endpoints ---

    @GetMapping("/campaigns/active")
    public ResponseEntity<List<com.adsphere.dto.campaign.CampaignResponse>> getActiveCampaigns() {
        return ResponseEntity.ok(campaignService.getActiveCampaignsWithCreatives());
    }

    // --- Website endpoints ---

    @PostMapping("/websites")
    public ResponseEntity<WebsiteResponse> register(@Valid @RequestBody WebsiteRequest request,
                                                     @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(websiteService.register(user.getUsername(), request));
    }

    @GetMapping("/websites")
    public ResponseEntity<List<WebsiteResponse>> getMyWebsites(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(websiteService.getMyWebsites(user.getUsername()));
    }

    @GetMapping("/websites/{id}")
    public ResponseEntity<WebsiteResponse> getById(@PathVariable Long id,
                                                    @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(websiteService.getById(id, user.getUsername()));
    }

    @PutMapping("/websites/{id}")
    public ResponseEntity<WebsiteResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody WebsiteRequest request,
                                                   @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(websiteService.update(id, user.getUsername(), request));
    }

    @DeleteMapping("/websites/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails user) {
        websiteService.delete(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }

    // --- Upgrade request endpoints ---

    @PostMapping("/upgrade-requests")
    public ResponseEntity<UpgradeResponse> submitUpgrade(@Valid @RequestBody UpgradeRequestDto dto,
                                                          @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(upgradeService.submit(user.getUsername(), dto));
    }

    @GetMapping("/upgrade-requests")
    public ResponseEntity<List<UpgradeResponse>> getMyUpgradeRequests(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(upgradeService.getMyRequests(user.getUsername()));
    }

    // --- Placement endpoints ---

    @PostMapping("/placements")
    public ResponseEntity<PlacementResponse> createPlacement(@Valid @RequestBody PlacementRequest request,
                                                              @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(placementService.create(user.getUsername(), request));
    }

    @GetMapping("/websites/{websiteId}/placements")
    public ResponseEntity<List<PlacementResponse>> getPlacementsByWebsite(@PathVariable Long websiteId,
                                                                            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(placementService.getByWebsite(websiteId, user.getUsername()));
    }

    @PutMapping("/placements/{placementId}/toggle")
    public ResponseEntity<PlacementResponse> togglePlacement(@PathVariable Long placementId,
                                                              @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(placementService.toggleActive(placementId, user.getUsername()));
    }

    // --- Earnings ---

    @GetMapping("/earnings")
    public ResponseEntity<BigDecimal> getEarnings(@AuthenticationPrincipal UserDetails userDetails) {
        Long publisherId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found")).getId();
        return ResponseEntity.ok(revenueService.getPublisherEarnings(publisherId));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<RevenueResponse>> getTransactions(@AuthenticationPrincipal UserDetails userDetails) {
        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(revenueService.getTransactionsByPublisher(user));
    }

    // --- Analytics ---

    @GetMapping("/websites/{id}/analytics")
    public ResponseEntity<AnalyticsSummary> getWebsiteAnalytics(
            @PathVariable Long id,
            @RequestParam String from,
            @RequestParam String to) {
        return ResponseEntity.ok(analyticsService.getWebsiteSummary(
                id, java.time.LocalDate.parse(from), java.time.LocalDate.parse(to)));
    }

    @GetMapping("/websites/{id}/analytics/daily")
    public ResponseEntity<List<DailyAnalytics>> getWebsiteDaily(
            @PathVariable Long id,
            @RequestParam String from,
            @RequestParam String to) {
        return ResponseEntity.ok(analyticsService.getWebsiteDaily(
                id, java.time.LocalDate.parse(from), java.time.LocalDate.parse(to)));
    }

    @GetMapping("/analytics/daily")
    public ResponseEntity<List<DailyAnalytics>> getGlobalPublisherDaily(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String from,
            @RequestParam String to) {
        Long publisherId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found")).getId();
        return ResponseEntity.ok(analyticsService.getGlobalPublisherDaily(
                publisherId, java.time.LocalDate.parse(from), java.time.LocalDate.parse(to)));
    }
}
