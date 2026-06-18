package com.adsphere.repository;

import com.adsphere.model.AdCreative;
import com.adsphere.model.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdCreativeRepository extends JpaRepository<AdCreative, Long> {
    List<AdCreative> findByCampaign(Campaign campaign);
}
