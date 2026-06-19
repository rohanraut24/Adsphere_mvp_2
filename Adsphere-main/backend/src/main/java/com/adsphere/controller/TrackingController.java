package com.adsphere.controller;

import com.adsphere.dto.placement.RevenueResponse;
import com.adsphere.service.RevenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/track")
@RequiredArgsConstructor
public class TrackingController {

    private final RevenueService revenueService;
    private final com.adsphere.service.PlacementService placementService;

    @PostMapping("/impression/{placementId}")
    public ResponseEntity<Void> impression(@PathVariable Long placementId) {
        revenueService.recordImpression(placementId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/click/{placementId}")
    public ResponseEntity<RevenueResponse> click(@PathVariable Long placementId) {
        return ResponseEntity.ok(revenueService.recordClick(placementId));
    }

    @GetMapping("/serve/{placementId}")
    public ResponseEntity<com.adsphere.dto.placement.AdServeResponse> serve(@PathVariable Long placementId) {
        // Fetch placement to get creative details
        // Note: For a real production system, this should check if placement is active
        com.adsphere.model.AdPlacement placement = placementService.getPlacementEntity(placementId);
        com.adsphere.model.AdCreative creative = placement.getAdCreative();
        
        if (creative == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Also record an impression when the ad is served
        revenueService.recordImpression(placementId);
        
        return ResponseEntity.ok(com.adsphere.dto.placement.AdServeResponse.builder()
                .title(creative.getTitle())
                .description(creative.getDescription())
                .imageUrl(creative.getImageUrl())
                .destinationUrl(creative.getDestinationUrl())
                .build());
    }
}
