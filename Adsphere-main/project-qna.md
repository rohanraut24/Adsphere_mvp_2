# AdSphere Project Review: Q&A and Cross-Examination Reference

This document compiles potential questions, deep technical inquiries, unique selling propositions (USPs), and mock cross-examination questions for a project review or defense of the **AdSphere** ad network.

---

## 💡 Part 1: Product, USP, & Competitive Advantage

### Q1: What is AdSphere and what problem does it solve?
**Answer:** AdSphere is a self-serve digital advertising network connecting Advertisers looking to market products with Publishers looking to monetize website traffic. It solves transparency and complexity issues in traditional ad networks by providing real-time financial tracking, simplified scriptless integration, and robust automated fraud detection in a single portal.

### Q2: What is the Unique Selling Proposition (USP) of AdSphere?
**Answer:** 
1. **Real-time Automated Fraud Pipeline**: Click-spam, bot-attacks, crawling traffic, and abnormal CTR patterns are blocked/flagged in real-time.
2. **Simplified Domain-Based Integration**: Publishers do not need to hardcode specific IDs into their sites. The ad slot dynamically infers the site identity using the browser's origin (`window.location.origin`).
3. **Transparent Instant Revenue Split**: 70% goes to the publisher, 20% to the network, and 10% to the platform immediately upon a legitimate click, recorded on immutable transaction ledgers.

### Q3: How is AdSphere better than platforms like Google AdSense or alternative ad networks?
**Answer:**
* **Immediate Approvals and Operations**: Unlike AdSense, which has opaque, weeks-long approval cycles, AdSphere provides a dedicated admin moderation queue allowing swift approvals.
* **Aggressive Real-Time Click Spam Protection**: Many networks run fraud checks on a nightly basis, charging advertisers first and refunding them days later. AdSphere performs real-time rate-limiting and User-Agent bot checks *before* deducting campaign budgets.
* **Low Integration Barrier**: AdSphere does not require publisher developers to customize scripts with complex parameters. Ad slots automatically load their appropriate configurations dynamically.

---

## ⚙️ Part 2: Technical Architecture & Design Decisions

### Q4: Explain the Dynamic Matching Algorithm. How does the system choose which ad to serve?
**Answer:** When an ad placement is set to "Dynamic Match", the backend runs a **Highest-Bid Auction model**:
1. It queries the database for all campaigns with status `ACTIVE`.
2. It filters out any campaigns whose remaining budget is less than their `cpcBid` (cost-per-click bid).
3. Out of the remaining eligible campaigns, it selects the campaign with the maximum `cpcBid` value.
4. It resolves the campaign's active creative and serves it.
*This maximizes platform revenue (higher CPC means larger network/platform cuts) and guarantees publishers get the highest-paying ad available.*

### Q5: Why did you transition from websiteId parameters to domain-based serving?
**Answer:** 
* **User Experience (UX)**: Requiring publishers to copy-paste unique database IDs (`websiteId=14`) into HTML scripts is error-prone.
* **Security & Simplicity**: By fetching the origin dynamically (`window.location.origin`), the frontend requests ads using the domain name itself. The backend resolves the website ID on-the-fly, preventing users from spoofing another publisher's traffic by simply swapping IDs.

### Q6: How does the system handle concurrent updates on analytics (e.g. many impressions hitting the server simultaneously)?
**Answer:** High-traffic ad serving causes concurrency issues (like `NonUniqueResultException` when writing daily analytics). AdSphere handles this via:
* Synchronized update methods (`updateAnalytics`).
* An automated merging/de-duplication routine: If concurrent threads create multiple analytics rows for the same day, the backend merges their impressions/clicks into a single row and deletes duplicate entries to maintain data integrity.

---

## 🔒 Part 3: Security & Fraud Prevention Pipeline

### Q7: How does the Click Rate-Limiting and Bot Detection work?
**Answer:** 
* **Bot Identification**: Inspects the `User-Agent` header for automated clients (e.g., Selenium, Puppeteer, headless Chrome, Python, curl).
* **Click Rate-Limiting**: Blocks clicks from the same IP address on a single placement if they occur less than **3 seconds** apart to prevent click spam.
* **Bot Click-Flooding**: If an IP clicks a placement more than **5 times within 60 seconds**, it is flagged as a high-severity `BOT_ATTACK`.
* **Impression Flooding**: Blocks impression recording if an IP requests ads/refreshes more than **30 times in 60 seconds** to keep CTR statistics clean.

### Q8: What is "Silent Discarding" and why is it preferred over throwing a HTTP error code (e.g., 403 Forbidden)?
**Answer:** If we return a `403 Forbidden` or a `429 Too Many Requests` error, bot developers and fraudulent publishers immediately know their scripts have been detected, allowing them to modify their behavior (e.g., rotate proxies or alter delays). By returning a mock `200 OK` response with `$0.00` share payouts, the attacker's script believes it succeeded, while advertiser budgets and publisher balances remain completely unaffected.

---

## 🛑 Part 4: Mock Cross-Examination (Grill Questions)

### Q9: Since you keep fraud tracking (like click/impression timestamps) in memory, what happens if the server restarts?
**Answer:** Because the rate-limiting metrics (IP and timestamps) are stored in thread-safe concurrent maps (`ConcurrentHashMap`), a server restart will clear this memory. However:
1. This is a standard trade-off to keep ad serving latency sub-millisecond (writing rate-limits to DB on every impression would crash database performance).
2. The platform's **Historical Anomaly Engine** serves as a safety net. It runs daily checks to detect abnormal CTRs (e.g., >25% CTR over 50 impressions), ensuring that even if a bot bypasses the short-term rate limiter during a restart, the publisher is flagged, and the admin is notified to suspend their account.

### Q10: How do you prevent IP spoofing via the `X-Forwarded-For` header?
**Answer:** Currently, we extract the first IP address from the `X-Forwarded-For` header. In a production environment behind a reverse proxy (like Nginx, Cloudflare, or AWS ALB), we would configure the proxy to strip client-supplied `X-Forwarded-For` headers and rewrite them with the trusted proxy header, ensuring clients cannot forge their IP.

### Q11: What happens to active placements if a Publisher is suspended?
**Answer:** The system features strict validation at the request level. In `TrackingController`:
* When serving ads, it verifies `placement.getWebsite().getStatus() == WebsiteStatus.APPROVED`.
* If a publisher is suspended, the admin panel sets their status to `SUSPENDED`, and their websites are filtered out. The serving endpoint automatically returns a 404 or falls back to basic placeholders, ensuring no ads are served on unapproved/suspended accounts.

### Q12: How do you prevent double-spending if a campaign budget runs out midway through a high-frequency click flood?
**Answer:** In `RevenueService.recordClick`, the budget deduction is fully transactional. It fetches the campaign, subtracts the CPC bid, and checks if the remaining budget is less than zero:
```java
BigDecimal newBudget = campaign.getBudget().subtract(cpc);
if (newBudget.compareTo(BigDecimal.ZERO) < 0) {
    throw new IllegalStateException("Campaign budget exhausted");
}
```
If the budget goes below zero, it throws an exception, rolling back the database transaction. This prevents any advertiser from being charged more than their balance, and automatically marks the campaign status as `COMPLETED`.
