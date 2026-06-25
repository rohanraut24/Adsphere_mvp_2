package com.adsphere.service;

import com.adsphere.dto.campaign.*;
import com.adsphere.model.*;
import com.adsphere.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final AdCreativeRepository adCreativeRepository;
    private final UserRepository userRepository;
    private final AdPlacementRepository adPlacementRepository;

    // --- Campaign CRUD ---

    public CampaignResponse create(String advertiserEmail, CampaignRequest request) {
        User advertiser = getUser(advertiserEmail);
        
        java.math.BigDecimal budget = request.getBudget();
        if (advertiser.getBalance().compareTo(budget) < 0) {
            throw new IllegalStateException("Insufficient wallet balance to allocate campaign budget");
        }
        advertiser.setBalance(advertiser.getBalance().subtract(budget));
        userRepository.save(advertiser);

        Campaign campaign = new Campaign();
        campaign.setAdvertiser(advertiser);
        mapRequest(campaign, request);
        campaign.setStatus(CampaignStatus.DRAFT);
        return toResponse(campaignRepository.save(campaign));
    }

    public List<CampaignResponse> getMyCampaigns(String advertiserEmail) {
        return campaignRepository.findByAdvertiser(getUser(advertiserEmail))
                .stream().map(this::toResponse).toList();
    }

    public CampaignResponse getById(Long id, String advertiserEmail) {
        return toResponse(findOwned(id, advertiserEmail));
    }

    public CampaignResponse update(Long id, String advertiserEmail, CampaignRequest request) {
        Campaign campaign = findOwned(id, advertiserEmail);
        if (campaign.getStatus() != CampaignStatus.DRAFT)
            throw new IllegalStateException("Only DRAFT campaigns can be edited");
        
        User advertiser = campaign.getAdvertiser();
        java.math.BigDecimal oldBudget = campaign.getBudget();
        java.math.BigDecimal newBudget = request.getBudget();
        java.math.BigDecimal diff = newBudget.subtract(oldBudget);
        
        if (diff.compareTo(java.math.BigDecimal.ZERO) > 0) {
            if (advertiser.getBalance().compareTo(diff) < 0) {
                throw new IllegalStateException("Insufficient wallet balance to increase budget");
            }
            advertiser.setBalance(advertiser.getBalance().subtract(diff));
        } else if (diff.compareTo(java.math.BigDecimal.ZERO) < 0) {
            advertiser.setBalance(advertiser.getBalance().add(diff.abs()));
        }
        userRepository.save(advertiser);

        mapRequest(campaign, request);
        return toResponse(campaignRepository.save(campaign));
    }

    public void delete(Long id, String advertiserEmail) {
        Campaign campaign = findOwned(id, advertiserEmail);
        if (campaign.getStatus() != CampaignStatus.DRAFT)
            throw new IllegalStateException("Only DRAFT campaigns can be deleted");
        
        // Refund campaign budget to advertiser's wallet
        User advertiser = campaign.getAdvertiser();
        advertiser.setBalance(advertiser.getBalance().add(campaign.getBudget()));
        userRepository.save(advertiser);

        campaignRepository.delete(campaign);
    }

    public CampaignResponse submitForApproval(Long id, String advertiserEmail) {
        Campaign campaign = findOwned(id, advertiserEmail);
        if (campaign.getStatus() != CampaignStatus.DRAFT)
            throw new IllegalStateException("Only DRAFT campaigns can be submitted");
        campaign.setStatus(CampaignStatus.PENDING_APPROVAL);
        return toResponse(campaignRepository.save(campaign));
    }

    public CampaignResponse pause(Long id, String advertiserEmail) {
        Campaign campaign = findOwned(id, advertiserEmail);
        if (campaign.getStatus() != CampaignStatus.ACTIVE)
            throw new IllegalStateException("Only ACTIVE campaigns can be paused");
        campaign.setStatus(CampaignStatus.PAUSED);
        return toResponse(campaignRepository.save(campaign));
    }

    public CampaignResponse resume(Long id, String advertiserEmail) {
        Campaign campaign = findOwned(id, advertiserEmail);
        if (campaign.getStatus() != CampaignStatus.PAUSED)
            throw new IllegalStateException("Only PAUSED campaigns can be resumed");
        campaign.setStatus(CampaignStatus.ACTIVE);
        return toResponse(campaignRepository.save(campaign));
    }

    // --- Network Admin actions ---

    public List<CampaignResponse> getPendingCampaigns() {
        return campaignRepository.findByStatus(CampaignStatus.PENDING_APPROVAL)
                .stream().map(this::toResponse).toList();
    }

    public CampaignResponse approve(Long id) {
        return setStatus(id, CampaignStatus.ACTIVE);
    }

    public CampaignResponse reject(Long id) {
        return setStatus(id, CampaignStatus.REJECTED);
    }

    public List<CampaignResponse> getActiveCampaignsWithCreatives() {
        return campaignRepository.findByStatus(CampaignStatus.ACTIVE).stream()
                .map(campaign -> {
                    CampaignResponse response = toResponse(campaign);
                    List<AdCreativeResponse> creatives = adCreativeRepository.findByCampaign(campaign)
                            .stream().map(this::toCreativeResponse).toList();
                    response.setCreatives(creatives);
                    return response;
                }).toList();
    }

    // --- AdCreative management ---

    public AdCreativeResponse addCreative(Long campaignId, String advertiserEmail, AdCreativeRequest request) {
        Campaign campaign = findOwned(campaignId, advertiserEmail);
        AdCreative creative = new AdCreative();
        creative.setCampaign(campaign);
        creative.setTitle(request.getTitle());
        creative.setDescription(request.getDescription());
        creative.setImageUrl(request.getImageUrl());
        creative.setDestinationUrl(request.getDestinationUrl());
        return toCreativeResponse(adCreativeRepository.save(creative));
    }

    public List<AdCreativeResponse> getCreatives(Long campaignId, String advertiserEmail) {
        Campaign campaign = findOwned(campaignId, advertiserEmail);
        return adCreativeRepository.findByCampaign(campaign)
                .stream().map(this::toCreativeResponse).toList();
    }

    public void deleteCreative(Long campaignId, Long creativeId, String advertiserEmail) {
        findOwned(campaignId, advertiserEmail);
        AdCreative creative = adCreativeRepository.findById(creativeId)
                .orElseThrow(() -> new IllegalArgumentException("Creative not found"));
        
        // Nullify references in placements to prevent foreign key constraint violations
        List<AdPlacement> placements = adPlacementRepository.findByAdCreative(creative);
        for (AdPlacement placement : placements) {
            placement.setAdCreative(null);
            adPlacementRepository.save(placement);
        }
        
        adCreativeRepository.delete(creative);
    }

    // --- Helpers ---

    private void mapRequest(Campaign c, CampaignRequest r) {
        c.setName(r.getName());
        c.setDescription(r.getDescription());
        c.setBudget(r.getBudget());
        c.setCpcBid(r.getCpcBid());
        c.setStartDate(r.getStartDate());
        c.setEndDate(r.getEndDate());
    }

    private CampaignResponse setStatus(Long id, CampaignStatus status) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        campaign.setStatus(status);
        return toResponse(campaignRepository.save(campaign));
    }

    private Campaign findOwned(Long id, String email) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        if (!campaign.getAdvertiser().getEmail().equals(email))
            throw new IllegalArgumentException("Access denied");
        return campaign;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private CampaignResponse toResponse(Campaign c) {
        CampaignResponse r = new CampaignResponse();
        r.setId(c.getId());
        r.setName(c.getName());
        r.setDescription(c.getDescription());
        r.setBudget(c.getBudget());
        r.setCpcBid(c.getCpcBid());
        r.setStartDate(c.getStartDate());
        r.setEndDate(c.getEndDate());
        r.setStatus(c.getStatus());
        r.setAdvertiserEmail(c.getAdvertiser().getEmail());
        r.setCreatedAt(c.getCreatedAt());
        return r;
    }

    private AdCreativeResponse toCreativeResponse(AdCreative c) {
        AdCreativeResponse r = new AdCreativeResponse();
        r.setId(c.getId());
        r.setCampaignId(c.getCampaign().getId());
        r.setTitle(c.getTitle());
        r.setDescription(c.getDescription());
        r.setImageUrl(c.getImageUrl());
        r.setDestinationUrl(c.getDestinationUrl());
        r.setCreatedAt(c.getCreatedAt());
        return r;
    }
}
