package com.adsphere.repository;

import com.adsphere.model.Campaign;
import com.adsphere.model.CampaignStatus;
import com.adsphere.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByAdvertiser(User advertiser);
    List<Campaign> findByStatus(CampaignStatus status);
    List<Campaign> findByAdvertiserAndStatus(User advertiser, CampaignStatus status);
}
