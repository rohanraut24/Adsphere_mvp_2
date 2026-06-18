package com.adsphere.repository;

import com.adsphere.model.AdPlacement;
import com.adsphere.model.Campaign;
import com.adsphere.model.Website;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdPlacementRepository extends JpaRepository<AdPlacement, Long> {
    List<AdPlacement> findByWebsite(Website website);
    List<AdPlacement> findByCampaign(Campaign campaign);
    List<AdPlacement> findByWebsiteAndActive(Website website, boolean active);
}
