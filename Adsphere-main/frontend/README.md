# AdSphere: Frontend Architecture & Flow 🎨

This document outlines the technical flow, architecture, and next steps for the AdSphere Angular frontend.

## 🏗 Frontend Architecture Flow
The frontend is the visually stunning, interactive face of AdSphere. It is built using **Angular 17 (Standalone Components)**, **Tailwind CSS**, and **Lucide Icons** for a premium, modern feel.

### 1. Authentication & Routing Flow
- **Login/Register:** Handled by `LoginComponent` and `RegisterComponent`.
- **State Management:** The `AuthService` stores the JWT token in `localStorage` and exposes the current user state via RxJS Observables.
- **Interceptors:** The `auth.interceptor.ts` automatically attaches the Bearer token to all outgoing API requests. Crucially, it intercepts `401 Unauthorized` and `403 Forbidden` errors (e.g., if an admin suspends the user mid-session) and smoothly routes them back to the login page.
- **Guards:** Angular Route Guards prevent Publishers from accessing Advertiser dashboards, ensuring role isolation.

### 2. Dashboard Rendering Flows
The UI is divided into role-specific modules:
- **Advertiser Dashboard:** Focuses on Campaign creation, budget tracking, and a dynamic Chart.js graph visualizing daily ad spend.
- **Publisher Dashboard:** Focuses on Website/Placement creation, monetization tracking, and a dynamic Chart.js graph visualizing daily earnings.
- **Network Admin Dashboard:** Acts as the moderation queue, utilizing split-view tables to rapidly approve or reject pending Websites and Campaigns.
- **Super Admin Platform Dashboard:** A macro-level view utilizing the `StatsComponent`. It pulls data from `api.service.ts` to render platform revenue, global CTR, and an automated **System Health & Anomalies** widget that dynamically turns red/yellow based on backend fraud alerts.

### 3. API Integration
- The `ApiService` centralizes all HTTP calls to the backend, grouping them logically (e.g., `api.publisher.*`, `api.admin.*`).
- Environment routing uses the Angular `proxy.conf.json` to seamlessly bypass CORS during local development.

---

## 🚀 Next Phase (Frontend)
The current frontend provides a flawless, premium user experience. The next phase will focus on advanced interactions, real-time data, and mobile responsiveness:

1. **WebSockets for Real-Time Metrics:** Upgrading the Chart.js graphs and dashboard numbers to update in real-time via WebSockets or Server-Sent Events (SSE) instead of relying on page reloads.
2. **Advanced Data Tables:** Implementing server-side pagination, sorting, and advanced filtering for the tables in the Admin and Network Admin dashboards.
3. **Dark Mode Implementation:** Leveraging Tailwind's dark mode utilities to provide a sleek, toggleable dark theme for power users.
4. **Interactive Ad Builder:** Adding a WYSIWYG (What You See Is What You Get) editor for Advertisers to design HTML5 display ads directly within the browser instead of just submitting text/links.
