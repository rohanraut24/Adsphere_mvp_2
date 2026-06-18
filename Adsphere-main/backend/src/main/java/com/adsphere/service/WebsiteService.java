package com.adsphere.service;

import com.adsphere.dto.website.*;
import com.adsphere.model.*;
import com.adsphere.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WebsiteService {

    private final WebsiteRepository websiteRepository;
    private final UserRepository userRepository;

    public WebsiteResponse register(String publisherEmail, WebsiteRequest request) {
        if (websiteRepository.existsByUrl(request.getUrl()))
            throw new IllegalArgumentException("URL already registered");

        User publisher = getUser(publisherEmail);

        Website website = new Website();
        website.setPublisher(publisher);
        website.setUrl(request.getUrl());
        website.setName(request.getName());
        website.setCategory(request.getCategory());
        website.setStatus(WebsiteStatus.PENDING);

        return toResponse(websiteRepository.save(website));
    }

    public List<WebsiteResponse> getMyWebsites(String publisherEmail) {
        User publisher = getUser(publisherEmail);
        return websiteRepository.findByPublisher(publisher).stream().map(this::toResponse).toList();
    }

    public WebsiteResponse getById(Long id, String publisherEmail) {
        return toResponse(findOwned(id, publisherEmail));
    }

    public WebsiteResponse update(Long id, String publisherEmail, WebsiteRequest request) {
        Website website = findOwned(id, publisherEmail);
        website.setName(request.getName());
        website.setCategory(request.getCategory());
        return toResponse(websiteRepository.save(website));
    }

    public void delete(Long id, String publisherEmail) {
        websiteRepository.delete(findOwned(id, publisherEmail));
    }

    // Network Admin / Super Admin actions
    public List<WebsiteResponse> getPendingWebsites() {
        return websiteRepository.findByStatus(WebsiteStatus.PENDING).stream().map(this::toResponse).toList();
    }

    public WebsiteResponse approve(Long id) {
        return setStatus(id, WebsiteStatus.APPROVED);
    }

    public WebsiteResponse reject(Long id) {
        return setStatus(id, WebsiteStatus.REJECTED);
    }

    private WebsiteResponse setStatus(Long id, WebsiteStatus status) {
        Website website = websiteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Website not found"));
        website.setStatus(status);
        return toResponse(websiteRepository.save(website));
    }

    private Website findOwned(Long id, String publisherEmail) {
        Website website = websiteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Website not found"));
        if (!website.getPublisher().getEmail().equals(publisherEmail))
            throw new IllegalArgumentException("Access denied");
        return website;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private WebsiteResponse toResponse(Website w) {
        WebsiteResponse r = new WebsiteResponse();
        r.setId(w.getId());
        r.setUrl(w.getUrl());
        r.setName(w.getName());
        r.setCategory(w.getCategory());
        r.setStatus(w.getStatus());
        r.setPublisherEmail(w.getPublisher().getEmail());
        r.setCreatedAt(w.getCreatedAt());
        return r;
    }
}
