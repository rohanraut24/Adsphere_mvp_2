package com.adsphere.controller;

import com.adsphere.dto.placement.RevenueResponse;
import com.adsphere.service.RevenueService;
import com.adsphere.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/track")
@RequiredArgsConstructor
public class TrackingController {

    private final RevenueService revenueService;
    private final com.adsphere.service.PlacementService placementService;
    private final com.adsphere.repository.WebsiteRepository websiteRepository;
    private final com.adsphere.repository.AdPlacementRepository adPlacementRepository;
    private final com.adsphere.repository.CampaignRepository campaignRepository;
    private final com.adsphere.repository.AdCreativeRepository adCreativeRepository;
    private final FraudDetectionService fraudDetectionService;

    @PostMapping("/impression/{placementId}")
    public ResponseEntity<Void> impression(
            @PathVariable Long placementId,
            HttpServletRequest request) {
        
        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        
        try {
            com.adsphere.model.AdPlacement placement = placementService.getPlacementEntity(placementId);
            if (fraudDetectionService.isImpressionSpam(placement, ip, userAgent)) {
                return ResponseEntity.ok().build();
            }
        } catch (Exception ignored) {}
        
        revenueService.recordImpression(placementId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/click/{placementId}")
    public ResponseEntity<RevenueResponse> click(
            @PathVariable Long placementId,
            @RequestParam(required = false) Long campaignId,
            HttpServletRequest request) {
        
        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        
        try {
            com.adsphere.model.AdPlacement placement = placementService.getPlacementEntity(placementId);
            if (fraudDetectionService.isClickFraudulent(placement, ip, userAgent)) {
                RevenueResponse mockResponse = new RevenueResponse();
                mockResponse.setPlacementId(placementId);
                mockResponse.setTotalAmount(java.math.BigDecimal.ZERO);
                mockResponse.setPublisherShare(java.math.BigDecimal.ZERO);
                mockResponse.setNetworkShare(java.math.BigDecimal.ZERO);
                mockResponse.setPlatformShare(java.math.BigDecimal.ZERO);
                mockResponse.setCreatedAt(java.time.LocalDateTime.now());
                return ResponseEntity.ok(mockResponse);
            }
        } catch (Exception ignored) {}
        
        return ResponseEntity.ok(revenueService.recordClick(placementId, campaignId));
    }

    @GetMapping("/serve/{placementId}")
    public ResponseEntity<com.adsphere.dto.placement.AdServeResponse> serve(
            @PathVariable Long placementId,
            HttpServletRequest request) {
        com.adsphere.model.AdPlacement placement = placementService.getPlacementEntity(placementId);
        
        if (!placement.isActive() || 
            placement.getWebsite().getStatus() != com.adsphere.model.WebsiteStatus.APPROVED) {
            return ResponseEntity.notFound().build();
        }

        com.adsphere.model.AdCreative creative = findCreativeForPlacement(placement);
        if (creative == null) {
            return ResponseEntity.notFound().build();
        }
        
        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        
        // Record impression when served, unless detected as spam
        if (!fraudDetectionService.isImpressionSpam(placement, ip, userAgent)) {
            revenueService.recordImpression(placementId, creative.getCampaign().getId());
        }
        
        return ResponseEntity.ok(com.adsphere.dto.placement.AdServeResponse.builder()
                .placementId(placement.getId())
                .campaignId(creative.getCampaign().getId())
                .title(creative.getTitle())
                .description(creative.getDescription())
                .imageUrl(creative.getImageUrl())
                .destinationUrl(creative.getDestinationUrl())
                .build());
    }

    @GetMapping("/serve")
    public ResponseEntity<List<com.adsphere.dto.placement.AdServeResponse>> serveBulk(
            @RequestParam(required = false) Long websiteId,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) Long campaignId,
            @RequestParam(required = false) Long placementId,
            HttpServletRequest request) {
        
        if (domain != null && !domain.trim().isEmpty()) {
            String cleanDomain = domain.endsWith("/") ? domain.substring(0, domain.length() - 1) : domain;
            com.adsphere.model.Website website = websiteRepository.findByUrl(cleanDomain)
                    .orElse(websiteRepository.findByUrl(cleanDomain + "/").orElse(null));
            if (website != null) {
                websiteId = website.getId();
            }
        }

        List<com.adsphere.model.AdPlacement> placements = new ArrayList<>();
        
        if (placementId != null) {
            try {
                placements.add(placementService.getPlacementEntity(placementId));
            } catch (Exception ignored) {}
        } else if (websiteId != null) {
            try {
                com.adsphere.model.Website website = websiteRepository.findById(websiteId).orElse(null);
                if (website != null) {
                    placements.addAll(adPlacementRepository.findByWebsite(website));
                }
            } catch (Exception ignored) {}
        }
        
        if (campaignId != null) {
            placements = placements.stream()
                    .filter(p -> p.getCampaign() != null && p.getCampaign().getId().equals(campaignId))
                    .collect(Collectors.toList());
        }
        
        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        List<com.adsphere.dto.placement.AdServeResponse> responses = new ArrayList<>();
        
        for (com.adsphere.model.AdPlacement placement : placements) {
            if (placement.isActive() && 
                placement.getWebsite().getStatus() == com.adsphere.model.WebsiteStatus.APPROVED) {
                
                com.adsphere.model.AdCreative creative = findCreativeForPlacement(placement);
                if (creative != null) {
                    // Record an impression for bulk serve (unless spam)
                    if (!fraudDetectionService.isImpressionSpam(placement, ip, userAgent)) {
                        revenueService.recordImpression(placement.getId(), creative.getCampaign().getId());
                    }
                    
                    responses.add(com.adsphere.dto.placement.AdServeResponse.builder()
                            .placementId(placement.getId())
                            .campaignId(creative.getCampaign().getId())
                            .title(creative.getTitle())
                            .description(creative.getDescription())
                            .imageUrl(creative.getImageUrl())
                            .destinationUrl(creative.getDestinationUrl())
                            .build());
                }
            }
        }
        
        return ResponseEntity.ok(responses);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private com.adsphere.model.AdCreative findCreativeForPlacement(com.adsphere.model.AdPlacement placement) {
        com.adsphere.model.Campaign campaign = placement.getCampaign();
        com.adsphere.model.AdCreative creative = placement.getAdCreative();

        if (campaign != null) {
            // Manual Placement: Verify campaign is active and has budget
            if (campaign.getStatus() != com.adsphere.model.CampaignStatus.ACTIVE ||
                campaign.getBudget().compareTo(campaign.getCpcBid()) < 0) {
                return null;
            }
            if (creative == null) {
                List<com.adsphere.model.AdCreative> creatives = adCreativeRepository.findByCampaign(campaign);
                if (!creatives.isEmpty()) {
                    creative = creatives.get(0);
                }
            }
        } else {
            // Dynamic Placement matching engine
            List<com.adsphere.model.Campaign> activeCampaigns = campaignRepository.findByStatus(com.adsphere.model.CampaignStatus.ACTIVE);
            List<com.adsphere.model.Campaign> fundedCampaigns = activeCampaigns.stream()
                    .filter(c -> c.getBudget().compareTo(c.getCpcBid()) >= 0)
                    .collect(Collectors.toList());
            
            if (fundedCampaigns.isEmpty()) {
                return null;
            }
            
            // Auction model: pick campaign with highest CPC bid
            com.adsphere.model.Campaign bestCampaign = fundedCampaigns.stream()
                    .max(java.util.Comparator.comparing(com.adsphere.model.Campaign::getCpcBid))
                    .orElse(null);
            
            if (bestCampaign != null) {
                List<com.adsphere.model.AdCreative> creatives = adCreativeRepository.findByCampaign(bestCampaign);
                if (!creatives.isEmpty()) {
                    creative = creatives.get(0);
                }
            }
        }

        return creative;
    }
}
