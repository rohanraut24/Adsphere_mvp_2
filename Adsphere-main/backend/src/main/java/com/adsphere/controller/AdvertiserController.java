package com.adsphere.controller;

import com.adsphere.dto.analytics.*;
import com.adsphere.dto.campaign.*;
import com.adsphere.dto.placement.RevenueResponse;
import com.adsphere.repository.UserRepository;
import com.adsphere.service.AnalyticsService;
import com.adsphere.service.CampaignService;
import com.adsphere.service.PlacementService;
import com.adsphere.service.RevenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/advertiser")
@RequiredArgsConstructor
public class AdvertiserController {

    private final CampaignService campaignService;
    private final PlacementService placementService;
    private final RevenueService revenueService;
    private final UserRepository userRepository;
    private final AnalyticsService analyticsService;

    // --- Campaign endpoints ---

    @PostMapping("/campaigns")
    public ResponseEntity<CampaignResponse> create(@Valid @RequestBody CampaignRequest request,
                                                    @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(campaignService.create(user.getUsername(), request));
    }

    @GetMapping("/campaigns")
    public ResponseEntity<List<CampaignResponse>> getMyCampaigns(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(campaignService.getMyCampaigns(user.getUsername()));
    }

    @GetMapping("/campaigns/{id}")
    public ResponseEntity<CampaignResponse> getById(@PathVariable Long id,
                                                     @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(campaignService.getById(id, user.getUsername()));
    }

    @PutMapping("/campaigns/{id}")
    public ResponseEntity<CampaignResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody CampaignRequest request,
                                                    @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(campaignService.update(id, user.getUsername(), request));
    }

    @DeleteMapping("/campaigns/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails user) {
        campaignService.delete(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/campaigns/{id}/submit")
    public ResponseEntity<CampaignResponse> submit(@PathVariable Long id,
                                                    @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(campaignService.submitForApproval(id, user.getUsername()));
    }

    @PutMapping("/campaigns/{id}/pause")
    public ResponseEntity<CampaignResponse> pause(@PathVariable Long id,
                                                   @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(campaignService.pause(id, user.getUsername()));
    }

    @PutMapping("/campaigns/{id}/resume")
    public ResponseEntity<CampaignResponse> resume(@PathVariable Long id,
                                                    @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(campaignService.resume(id, user.getUsername()));
    }

    // --- AdCreative endpoints ---

    @PostMapping("/campaigns/{id}/creatives")
    public ResponseEntity<AdCreativeResponse> addCreative(@PathVariable Long id,
                                                           @Valid @RequestBody AdCreativeRequest request,
                                                           @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(campaignService.addCreative(id, user.getUsername(), request));
    }

    @GetMapping("/campaigns/{id}/creatives")
    public ResponseEntity<List<AdCreativeResponse>> getCreatives(@PathVariable Long id,
                                                                  @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(campaignService.getCreatives(id, user.getUsername()));
    }

    @DeleteMapping("/campaigns/{campaignId}/creatives/{creativeId}")
    public ResponseEntity<Void> deleteCreative(@PathVariable Long campaignId,
                                                @PathVariable Long creativeId,
                                                @AuthenticationPrincipal UserDetails user) {
        campaignService.deleteCreative(campaignId, creativeId, user.getUsername());
        return ResponseEntity.noContent().build();
    }

    // --- Placement view ---

    @GetMapping("/campaigns/{id}/placements")
    public ResponseEntity<?> getCampaignPlacements(@PathVariable Long id,
                                                    @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(placementService.getByCampaign(id, user.getUsername()));
    }

    // --- Spend ---

    @GetMapping("/spend")
    public ResponseEntity<?> getSpend(@AuthenticationPrincipal UserDetails userDetails) {
        Long advertiserId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found")).getId();
        return ResponseEntity.ok(revenueService.getAdvertiserSpend(advertiserId));
    }

    @GetMapping("/transactions")
    public ResponseEntity<java.util.List<RevenueResponse>> getTransactions(@AuthenticationPrincipal UserDetails userDetails) {
        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(revenueService.getTransactionsByAdvertiser(user));
    }

    // --- Analytics ---

    @GetMapping("/campaigns/{id}/analytics")
    public ResponseEntity<AnalyticsSummary> getCampaignAnalytics(
            @PathVariable Long id,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().minusDays(30)}") String from,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now()}") String to) {
        return ResponseEntity.ok(analyticsService.getCampaignSummary(
                id, java.time.LocalDate.parse(from), java.time.LocalDate.parse(to)));
    }

    @GetMapping("/campaigns/{id}/analytics/daily")
    public ResponseEntity<java.util.List<DailyAnalytics>> getCampaignDaily(
            @PathVariable Long id,
            @RequestParam String from,
            @RequestParam String to) {
        return ResponseEntity.ok(analyticsService.getCampaignDaily(
                id, java.time.LocalDate.parse(from), java.time.LocalDate.parse(to)));
    }

    @GetMapping("/analytics/daily")
    public ResponseEntity<java.util.List<DailyAnalytics>> getGlobalAdvertiserDaily(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String from,
            @RequestParam String to) {
        Long advertiserId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found")).getId();
        return ResponseEntity.ok(analyticsService.getGlobalAdvertiserDaily(
                advertiserId, java.time.LocalDate.parse(from), java.time.LocalDate.parse(to)));
    }
}
