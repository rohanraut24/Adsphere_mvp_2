package com.adsphere.repository;

import com.adsphere.model.User;
import com.adsphere.model.Website;
import com.adsphere.model.WebsiteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WebsiteRepository extends JpaRepository<Website, Long> {
    List<Website> findByPublisher(User publisher);
    List<Website> findByStatus(WebsiteStatus status);
    List<Website> findByPublisherAndStatus(User publisher, WebsiteStatus status);
    boolean existsByUrl(String url);
}
