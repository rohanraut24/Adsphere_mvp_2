package com.adsphere.service;

import com.adsphere.dto.placement.*;
import com.adsphere.model.*;
import com.adsphere.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlacementService {

    private final AdPlacementRepository adPlacementRepository;
    private final WebsiteRepository websiteRepository;
    private final CampaignRepository campaignRepository;
    private final AdCreativeRepository adCreativeRepository;
    private final UserRepository userRepository;

    public PlacementResponse create(String publisherEmail, PlacementRequest request) {
        Website website = websiteRepository.findById(request.getWebsiteId())
                .orElseThrow(() -> new IllegalArgumentException("Website not found"));

        if (!website.getPublisher().getEmail().equals(publisherEmail))
            throw new IllegalArgumentException("Access denied");

        if (website.getStatus() != WebsiteStatus.APPROVED)
            throw new IllegalStateException("Website must be APPROVED before adding placements");

        Campaign campaign = null;
        if (request.getCampaignId() != null) {
            campaign = campaignRepository.findById(request.getCampaignId())
                    .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));

            if (campaign.getStatus() != CampaignStatus.ACTIVE)
                throw new IllegalStateException("Campaign must be ACTIVE to place ads");
        }

        AdCreative creative = null;
        if (request.getAdCreativeId() != null) {
            creative = adCreativeRepository.findById(request.getAdCreativeId())
                    .orElseThrow(() -> new IllegalArgumentException("AdCreative not found"));
        }

        AdPlacement placement = new AdPlacement();
        placement.setWebsite(website);
        placement.setCampaign(campaign);
        placement.setAdCreative(creative);
        placement.setActive(true);

        return toResponse(adPlacementRepository.save(placement));
    }

    public List<PlacementResponse> getByWebsite(Long websiteId, String publisherEmail) {
        Website website = websiteRepository.findById(websiteId)
                .orElseThrow(() -> new IllegalArgumentException("Website not found"));
        if (!website.getPublisher().getEmail().equals(publisherEmail))
            throw new IllegalArgumentException("Access denied");
        return adPlacementRepository.findByWebsite(website).stream().map(this::toResponse).toList();
    }

    public List<PlacementResponse> getByCampaign(Long campaignId, String advertiserEmail) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        if (!campaign.getAdvertiser().getEmail().equals(advertiserEmail))
            throw new IllegalArgumentException("Access denied");
        return adPlacementRepository.findByCampaign(campaign).stream().map(this::toResponse).toList();
    }

    public PlacementResponse toggleActive(Long placementId, String publisherEmail) {
        AdPlacement placement = adPlacementRepository.findById(placementId)
                .orElseThrow(() -> new IllegalArgumentException("Placement not found"));
        if (!placement.getWebsite().getPublisher().getEmail().equals(publisherEmail))
            throw new IllegalArgumentException("Access denied");
        placement.setActive(!placement.isActive());
        return toResponse(adPlacementRepository.save(placement));
    }

    public AdPlacement getPlacementEntity(Long placementId) {
        return adPlacementRepository.findById(placementId)
                .orElseThrow(() -> new IllegalArgumentException("Placement not found"));
    }

    private PlacementResponse toResponse(AdPlacement p) {
        PlacementResponse r = new PlacementResponse();
        r.setId(p.getId());
        r.setWebsiteId(p.getWebsite().getId());
        r.setWebsiteUrl(p.getWebsite().getUrl());
        r.setCampaignId(p.getCampaign() != null ? p.getCampaign().getId() : null);
        r.setCampaignName(p.getCampaign() != null ? p.getCampaign().getName() : "Dynamic Matching");
        r.setAdCreativeId(p.getAdCreative() != null ? p.getAdCreative().getId() : null);
        r.setActive(p.isActive());
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }
}
