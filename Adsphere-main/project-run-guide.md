# AdSphere Project Execution & Review Presentation Guide

This guide describes how to run all components of the AdSphere platform, how to resolve common dependency conflicts (such as the Angular CDK resolution issue), and provides a step-by-step presentation script to demonstrate the platform to reviewers to secure maximum marks.

---

## 🚀 How to Run the Project

### 1. Spring Boot Backend
1. Navigate to the backend directory:
   ```bash
   cd Adsphere-main/backend
   ```
2. Build and run the project:
   ```bash
   mvn spring-boot:run
   ```
   *The backend will boot up on port `8080`. You can access the H2 database console at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:adsphere`, Username: `sa`, no password).*

### 2. Angular Admin/Advertiser Portal
1. Navigate to the frontend directory:
   ```bash
   cd Adsphere-main/frontend
   ```
2. **Resolve Peer Dependency Issues**:
   If you run a standard `npm install`, you will encounter an `ERESOLVE` peer dependency mismatch between `ng2-charts` and the Angular Core/CDK libraries. Resolve this by running:
   ```bash
   npm install --legacy-peer-deps
   ```
   *This command tells npm to bypass strict peer dependency checks, which resolves versioning overlaps between chart packages and Angular 17.3.*
3. Run the development server:
   ```bash
   npm start
   ```
   *The main portal will open at `http://localhost:4200`.*

### 3. Publisher Site (React Integration)
1. Navigate to the publisher site directory:
   ```bash
   cd Adsphere-main/publisher-site
   ```
2. Install dependencies and start the app:
   ```bash
   npm install
   npm run dev
   ```
   *The publisher demonstration blog will open at `http://localhost:5173`.*

---

## 🎓 Reviewer Presentation Script (Maximize Your Marks)

Follow this structured script during your evaluation to showcase every premium feature and architectural decision.

### ⏱️ Stage 1: The Advertiser Flow & Wallet Funding (2 Mins)
* **Goal**: Show advertiser onboarding and budget allocation.
1. Register a new account. By default, the system grants the `ADVERTISER` role.
2. **Point Out**: Highlight the **Wallet widget** in the bottom-left corner of the sidebar. Point out that new users start with a default balance of `$1000.00`.
3. **Trigger Recharge**: Click **Recharge** in the sidebar, enter `$500`, and show that the wallet updates instantly to `$1500.00` on the screen.
4. **Create a Campaign**: Go to **Campaigns** -> **New Campaign**. Set a total budget of `$200` and a CPC bid of `$0.50`.
5. **Show Budget Lock**: Point out that the moment the campaign is created in `DRAFT`, `$200` is deducted from the wallet balance (reducing it to `$1300.00`) and locked into the campaign.

### ⏱️ Stage 2: Admin Queue & Approvals (2 Mins)
* **Goal**: Show role-based authorization and moderation.
1. Log out and log in with an Administrator account (or use the upgrade request modal to become admin).
2. Go to **Campaign Approvals** and approve the pending campaign.
3. Go to **Website Approvals** and show the moderation checks for registered publisher sites.

### ⏱️ Stage 3: Dynamic Domain Serving (2 Mins)
* **Goal**: Explain your custom ad delivery algorithms.
1. Log in as a `PUBLISHER` and register a website with URL `http://localhost:5173`.
2. Create an ad placement slot (e.g. Banner) and set it to **Dynamic Match**.
3. Open the Publisher blog site (`http://localhost:5173`) in another tab.
4. **Explain the Tech**: Show the reviewer that the blog dynamically renders ads without any hardcoded website IDs in the code! Explain that the React component reads `window.location.origin`, sends it to the bulk serve API, and the backend dynamically matches the domain to the website ID and retrieves the active placements in a single HTTP request.

### ⏱️ Stage 4: Real-time Earnings & Transactions (2 Mins)
* **Goal**: Demonstrate transactional integrity and payouts.
1. Click the ad on the blog site. It redirects to the advertiser's landing page.
2. Go to the Publisher dashboard and show:
   * Clicks and earnings updated immediately.
   * **70% revenue share** (e.g., `$0.35` for a `$0.50` CPC) credited directly to the publisher's wallet balance in real-time.
3. Go to the Advertiser dashboard and show:
   * The campaign budget has drained by `$0.50` (the CPC cost).
   * Real-time charts updated automatically.

### ⏱️ Stage 5: Live Fraud & Anomaly Defense (4 Mins)
* **Goal**: The "Wow Factor." This section shows the platform is secure.
1. **Click-Spam Simulation**: Click the ad on the blog site again immediately (within 3 seconds).
   * Open the browser Developer Tools -> **Network Tab** -> click the `/api/track/click` request.
   * **Show the reviewer**: The request returned `200 OK` (success) but all payout fields are `0.00`. Explain **Silent Discarding**: we do not alert the attacker that they are blocked, but the advertiser's campaign budget is safe and the publisher gets no payout.
2. **Bot Traffic Simulation**: Open your terminal and trigger a simulated bot click using `curl`:
   ```bash
   curl -X POST "http://localhost:8080/api/track/click/1" -H "User-Agent: curl/8.0.1"
   ```
   Show that it is silently discarded with zero payouts due to the command-line client user-agent.
3. **Admin Alerts**: Log in as Admin and scroll to **System Health & Anomalies**:
   * Show the live `CLICK_SPAM` and `BOT_USER_AGENT` alerts displaying the offending IP (`127.0.0.1`).
   * Show the **Suspend** action button next to the publisher alert, demonstrating administrative power to ban bad actors instantly.
