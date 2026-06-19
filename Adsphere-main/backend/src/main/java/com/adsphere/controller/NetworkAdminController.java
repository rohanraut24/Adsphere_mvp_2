package com.adsphere.controller;

import com.adsphere.dto.campaign.CampaignResponse;
import com.adsphere.dto.website.WebsiteResponse;
import com.adsphere.service.CampaignService;
import com.adsphere.service.WebsiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/network")
@RequiredArgsConstructor
public class NetworkAdminController {

    private final WebsiteService websiteService;
    private final CampaignService campaignService;
    private final com.adsphere.service.AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<com.adsphere.dto.admin.AdminStats> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/websites/pending")
    public ResponseEntity<List<WebsiteResponse>> getPendingWebsites() {
        return ResponseEntity.ok(websiteService.getPendingWebsites());
    }

    @PutMapping("/websites/{id}/approve")
    public ResponseEntity<WebsiteResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(websiteService.approve(id));
    }

    @PutMapping("/websites/{id}/reject")
    public ResponseEntity<WebsiteResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(websiteService.reject(id));
    }

    // --- Campaign approval ---

    @GetMapping("/campaigns/pending")
    public ResponseEntity<List<CampaignResponse>> getPendingCampaigns() {
        return ResponseEntity.ok(campaignService.getPendingCampaigns());
    }

    @PutMapping("/campaigns/{id}/approve")
    public ResponseEntity<CampaignResponse> approveCampaign(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.approve(id));
    }

    @PutMapping("/campaigns/{id}/reject")
    public ResponseEntity<CampaignResponse> rejectCampaign(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.reject(id));
    }
}
