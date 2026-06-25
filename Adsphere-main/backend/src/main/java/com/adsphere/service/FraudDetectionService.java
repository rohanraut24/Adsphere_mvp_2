package com.adsphere.service;

import com.adsphere.dto.admin.AnomalyReport;
import com.adsphere.model.AdPlacement;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FraudDetectionService {

    private final Map<String, Long> lastClickMap = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> clickWindowMap = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> impressionWindowMap = new ConcurrentHashMap<>();
    private final List<AnomalyReport> blockedFraudEvents = Collections.synchronizedList(new ArrayList<>());

    public boolean isClickFraudulent(AdPlacement placement, String ip, String userAgent) {
        if (placement == null) {
            return false;
        }

        if (isBotUserAgent(userAgent)) {
            addAnomaly(new AnomalyReport(
                    "HIGH", "BOT_USER_AGENT",
                    String.format("Bot click blocked from IP %s. User-Agent: %s", ip, userAgent),
                    "PUBLISHER", placement.getWebsite().getPublisher().getId()
            ));
            return true;
        }

        String key = ip + "_" + placement.getId();
        long now = System.currentTimeMillis();

        // 1. Click Rate Limiting (clicks < 3 seconds apart)
        Long lastClick = lastClickMap.put(key, now);
        if (lastClick != null && (now - lastClick) < 3000) {
            addAnomaly(new AnomalyReport(
                    "MEDIUM", "CLICK_SPAM",
                    String.format("Spam click blocked from IP %s (less than 3s gap).", ip),
                    "PUBLISHER", placement.getWebsite().getPublisher().getId()
            ));
            return true;
        }

        // 2. Click Volumetrics (more than 5 clicks in 60 seconds)
        List<Long> clicks = clickWindowMap.computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>()));
        synchronized (clicks) {
            clicks.add(now);
            clicks.removeIf(t -> (now - t) > 60000);
            if (clicks.size() > 5) {
                addAnomaly(new AnomalyReport(
                        "HIGH", "BOT_ATTACK",
                        String.format("Bot-like click volume from IP %s (%d clicks in 60s).", ip, clicks.size()),
                        "PUBLISHER", placement.getWebsite().getPublisher().getId()
                ));
                return true;
            }
        }

        return false;
    }

    public boolean isImpressionSpam(AdPlacement placement, String ip, String userAgent) {
        if (placement == null) {
            return false;
        }

        if (isBotUserAgent(userAgent)) {
            addAnomaly(new AnomalyReport(
                    "LOW", "BOT_USER_AGENT",
                    String.format("Bot crawling detected from IP %s.", ip),
                    "WEBSITE", placement.getWebsite().getId()
            ));
            return true;
        }

        String key = ip + "_" + placement.getId();
        long now = System.currentTimeMillis();

        // Impression Flooding Check (> 30 impressions in 60 seconds)
        List<Long> impressions = impressionWindowMap.computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>()));
        synchronized (impressions) {
            impressions.add(now);
            impressions.removeIf(t -> (now - t) > 60000);
            if (impressions.size() > 30) {
                addAnomaly(new AnomalyReport(
                        "LOW", "IMPRESSION_SPAM",
                        String.format("Impression flood from IP %s (%d impressions in 60s).", ip, impressions.size()),
                        "WEBSITE", placement.getWebsite().getId()
                ));
                return true;
            }
        }

        return false;
    }

    public List<AnomalyReport> getRealtimeAnomalies() {
        synchronized (blockedFraudEvents) {
            return new ArrayList<>(blockedFraudEvents);
        }
    }

    private boolean isBotUserAgent(String userAgent) {
        if (userAgent == null || userAgent.trim().isEmpty()) {
            return true;
        }
        String uaLower = userAgent.toLowerCase();
        return uaLower.contains("bot") || 
               uaLower.contains("crawler") || 
               uaLower.contains("spider") || 
               uaLower.contains("headless") || 
               uaLower.contains("phantom") || 
               uaLower.contains("selenium") || 
               uaLower.contains("puppeteer") || 
               uaLower.contains("python") || 
               uaLower.contains("curl") || 
               uaLower.contains("wget") ||
               uaLower.contains("httpclient");
    }

    private void addAnomaly(AnomalyReport report) {
        synchronized (blockedFraudEvents) {
            if (blockedFraudEvents.size() >= 100) {
                blockedFraudEvents.remove(0);
            }
            blockedFraudEvents.add(report);
        }
    }
}
