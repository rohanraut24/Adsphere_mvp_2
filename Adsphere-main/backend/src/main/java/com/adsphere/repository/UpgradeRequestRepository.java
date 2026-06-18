package com.adsphere.repository;

import com.adsphere.model.UpgradeRequest;
import com.adsphere.model.UpgradeStatus;
import com.adsphere.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UpgradeRequestRepository extends JpaRepository<UpgradeRequest, Long> {
    List<UpgradeRequest> findByUser(User user);
    List<UpgradeRequest> findByStatus(UpgradeStatus status);
    boolean existsByUserAndStatus(User user, UpgradeStatus status);
}
