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

    @PostMapping("/impression/{placementId}")
    public ResponseEntity<Void> impression(@PathVariable Long placementId) {
        revenueService.recordImpression(placementId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/click/{placementId}")
    public ResponseEntity<RevenueResponse> click(@PathVariable Long placementId) {
        return ResponseEntity.ok(revenueService.recordClick(placementId));
    }
}
