package com.adsphere.service;

import com.adsphere.dto.upgrade.*;
import com.adsphere.model.*;
import com.adsphere.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UpgradeService {

    private final UpgradeRequestRepository upgradeRequestRepository;
    private final UserRepository userRepository;

    public UpgradeResponse submit(String userEmail, UpgradeRequestDto dto) {
        User user = getUser(userEmail);

        if (upgradeRequestRepository.existsByUserAndStatus(user, UpgradeStatus.PENDING))
            throw new IllegalStateException("You already have a pending upgrade request");

        UpgradeRequest req = new UpgradeRequest();
        req.setUser(user);
        req.setRequestedRole(dto.getRequestedRole());
        req.setReason(dto.getReason());
        req.setStatus(UpgradeStatus.PENDING);

        return toResponse(upgradeRequestRepository.save(req));
    }

    public UpgradeResponse review(Long requestId, String adminEmail, ReviewRequest reviewRequest) {
        UpgradeRequest req = upgradeRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (req.getStatus() != UpgradeStatus.PENDING)
            throw new IllegalStateException("Request already reviewed");

        User admin = getUser(adminEmail);
        req.setStatus(reviewRequest.getDecision());
        req.setReviewNote(reviewRequest.getReviewNote());
        req.setReviewedBy(admin);
        req.setReviewedAt(LocalDateTime.now());

        if (reviewRequest.getDecision() == UpgradeStatus.APPROVED) {
            User user = req.getUser();
            user.setRole(req.getRequestedRole());
            userRepository.save(user);
        }

        return toResponse(upgradeRequestRepository.save(req));
    }

    public List<UpgradeResponse> getPending() {
        return upgradeRequestRepository.findByStatus(UpgradeStatus.PENDING)
                .stream().map(this::toResponse).toList();
    }

    public List<UpgradeResponse> getMyRequests(String userEmail) {
        return upgradeRequestRepository.findByUser(getUser(userEmail))
                .stream().map(this::toResponse).toList();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private UpgradeResponse toResponse(UpgradeRequest r) {
        UpgradeResponse res = new UpgradeResponse();
        res.setId(r.getId());
        res.setUserEmail(r.getUser().getEmail());
        res.setRequestedRole(r.getRequestedRole());
        res.setStatus(r.getStatus());
        res.setReason(r.getReason());
        res.setReviewNote(r.getReviewNote());
        res.setReviewedByEmail(r.getReviewedBy() != null ? r.getReviewedBy().getEmail() : null);
        res.setCreatedAt(r.getCreatedAt());
        res.setReviewedAt(r.getReviewedAt());
        return res;
    }
}
