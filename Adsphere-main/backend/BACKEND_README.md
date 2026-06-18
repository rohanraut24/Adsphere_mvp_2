# AdSphere: Backend Architecture & Flow ⚙️

This document outlines the technical flow, architecture, and next steps for the AdSphere Spring Boot backend.

## 🏗 Backend Architecture Flow
The backend serves as the secure, high-performance engine for the AdSphere advertising network. It is built using **Java 17**, **Spring Boot 3**, **Spring Security (JWT)**, and **Spring Data JPA**.

### 1. Security & Authentication Flow
- **Registration/Login:** Handled by `AuthController`. Passwords are encrypted using BCrypt.
- **JWT Tokens:** Upon successful login, the `JwtUtil` issues a stateless JWT token containing the user's email and role.
- **Authorization:** `SecurityConfig` enforces endpoint restrictions based on roles (e.g., `/api/admin/**` is strictly `SUPER_ADMIN`).
- **Suspension Enforcement:** `UserDetailsServiceImpl` checks `UserStatus.ACTIVE`. If a user is suspended, the `JwtAuthFilter` immediately rejects their token and throws a `DisabledException`, mapping to a `403 Forbidden` response.

### 2. Core Entity Flows
- **Campaigns (Advertisers):** Advertisers create Campaigns. They sit in `PENDING` until a Network Admin approves them. Clicks deduct funds from the campaign's `budget`.
- **Websites (Publishers):** Publishers register Websites. Network Admins review and approve them. Only approved websites can host `AdPlacements`.
- **Placements & Tracking:** Placements link a Campaign to a Website.
  - `GET /api/track/click/{placementId}`: Records a click, deducts the `cpcBid` from the campaign budget, increments global analytics, and generates a `RevenueTransaction`.
  - `RevenueTransaction`: Automatically splits the CPC into Publisher (70%), Network (20%), and Platform (10%) shares.

### 3. Anomaly & Fraud Detection Flow
- The `AdminService` queries the `AnalyticsRepository` to dynamically generate an `AnomalyReport`.
- It detects **Click Fraud** (CTR > 25%), **Dead Inventory** (High impressions, 0 clicks), and **Exhausted Budgets**, returning them in the `AdminStats` payload.

---

## 🚀 Next Phase (Backend)
The current backend handles the core network mechanics flawlessly. The next phase will focus on scale, performance, and advanced matching algorithms:

1. **Redis Caching for Ad Serving:** Moving the `/api/track` and Ad Serving endpoints to use Redis instead of hitting the primary SQL database on every single impression. This will massively increase throughput.
2. **AI-Driven Ad Matching Algorithm:** Implementing a scoring system (using tags, CTR history, and advertiser budgets) to automatically serve the highest-converting ad to a given placement.
3. **Geo-Location & IP Blocking:** Integrating an IP parsing library to block fraudulent IPs and allow advertisers to Geo-Target their campaigns (e.g., "Only show in US and UK").
4. **Asynchronous Processing:** Offloading the Revenue Transaction splitting to a message broker (like RabbitMQ or Kafka) so that tracking API responses are instant.
