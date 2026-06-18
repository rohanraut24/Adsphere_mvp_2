# AdSphere Frontend - Complete Build Prompt

## Project Overview
AdSphere is a multi-role advertising platform. The backend is a Spring Boot REST API running at `http://localhost:8080`. All protected endpoints require a Bearer JWT token in the `Authorization` header.

---

## Tech Stack (Recommended)
- React 18 + Vite
- React Router v6
- Axios (with interceptor to attach JWT)
- TailwindCSS
- Recharts (for analytics graphs)
- React Hook Form + Zod (form validation)

---

## Authentication

### Base URL: `http://localhost:8080/api/auth`

### Register
```
POST /api/auth/register
Body: { fullName, email, password }
Response: { token, email, role }
```
- Default role on register is always `ADVERTISER`
- Store token in localStorage

### Login
```
POST /api/auth/login
Body: { email, password }
Response: { token, email, role }
```
- Decode JWT or use the `role` field from response to determine which dashboard to show

### Roles
| Role | Dashboard |
|---|---|
| SUPER_ADMIN | Admin Panel |
| NETWORK_ADMIN | Network Panel |
| PUBLISHER | Publisher Dashboard |
| ADVERTISER | Advertiser Dashboard |

---

## Axios Setup
```js
// All requests must include:
Authorization: Bearer <token>
Content-Type: application/json
```

---

## Role-Based Routing
Protect routes by role. After login, redirect to:
- `SUPER_ADMIN` → `/admin`
- `NETWORK_ADMIN` → `/network`
- `PUBLISHER` → `/publisher`
- `ADVERTISER` → `/advertiser`

---

## ADVERTISER Dashboard

### Campaigns — `/api/advertiser/campaigns`

| Action | Method | Endpoint |
|---|---|---|
| List my campaigns | GET | `/api/advertiser/campaigns` |
| Get campaign | GET | `/api/advertiser/campaigns/{id}` |
| Create campaign | POST | `/api/advertiser/campaigns` |
| Update campaign | PUT | `/api/advertiser/campaigns/{id}` |
| Delete campaign | DELETE | `/api/advertiser/campaigns/{id}` |
| Submit for approval | PUT | `/api/advertiser/campaigns/{id}/submit` |
| Pause campaign | PUT | `/api/advertiser/campaigns/{id}/pause` |
| Resume campaign | PUT | `/api/advertiser/campaigns/{id}/resume` |

**Campaign Create/Update Body:**
```json
{
  "name": "string",
  "description": "string",
  "budget": 100.00,
  "cpcBid": 0.10,
  "startDate": "2024-01-01",
  "endDate": "2024-12-31"
}
```

**Campaign Response:**
```json
{
  "id": 1,
  "name": "string",
  "description": "string",
  "budget": 100.00,
  "cpcBid": 0.10,
  "startDate": "2024-01-01",
  "endDate": "2024-12-31",
  "status": "DRAFT | PENDING_APPROVAL | ACTIVE | PAUSED | COMPLETED | REJECTED",
  "advertiserEmail": "string",
  "createdAt": "datetime"
}
```

**Campaign Status Flow:**
```
DRAFT → [submit] → PENDING_APPROVAL → [network approves] → ACTIVE
ACTIVE → [pause] → PAUSED → [resume] → ACTIVE
ACTIVE → [budget=0] → COMPLETED (auto)
PENDING_APPROVAL → [network rejects] → REJECTED
```
- Only DRAFT campaigns can be edited or deleted
- Show action buttons conditionally based on status

### Ad Creatives — nested under campaigns

| Action | Method | Endpoint |
|---|---|---|
| List creatives | GET | `/api/advertiser/campaigns/{id}/creatives` |
| Add creative | POST | `/api/advertiser/campaigns/{id}/creatives` |
| Delete creative | DELETE | `/api/advertiser/campaigns/{campaignId}/creatives/{creativeId}` |

**Creative Body:**
```json
{
  "title": "string",
  "description": "string",
  "imageUrl": "string",
  "destinationUrl": "string"
}
```

### Placements View
```
GET /api/advertiser/campaigns/{id}/placements
```
Read-only. Shows which websites are serving this campaign.

### Spend & Transactions
```
GET /api/advertiser/spend           → BigDecimal (total spend)
GET /api/advertiser/transactions    → list of revenue transactions
```

### Analytics
```
GET /api/advertiser/campaigns/{id}/analytics?from=2024-01-01&to=2024-12-31
Response: { totalImpressions, totalClicks, ctr, totalRevenue }

GET /api/advertiser/campaigns/{id}/analytics/daily?from=2024-01-01&to=2024-12-31
Response: [{ date, impressions, clicks }]
```
- Use a line/bar chart for daily breakdown (Recharts recommended)

### Upgrade Request (to become PUBLISHER or NETWORK_ADMIN)
```
POST /api/publisher/upgrade-requests
Body: { requestedRole: "PUBLISHER", reason: "string" }

GET /api/publisher/upgrade-requests  → list my upgrade requests
```
Note: upgrade endpoints are under `/api/publisher/` path even for advertisers.

---

## PUBLISHER Dashboard

### Websites — `/api/publisher/websites`

| Action | Method | Endpoint |
|---|---|---|
| List my websites | GET | `/api/publisher/websites` |
| Get website | GET | `/api/publisher/websites/{id}` |
| Register website | POST | `/api/publisher/websites` |
| Update website | PUT | `/api/publisher/websites/{id}` |
| Delete website | DELETE | `/api/publisher/websites/{id}` |

**Website Body:**
```json
{ "url": "https://mysite.com", "name": "My Site", "category": "Tech" }
```

**Website Response:**
```json
{
  "id": 1,
  "url": "string",
  "name": "string",
  "category": "string",
  "status": "PENDING | APPROVED | REJECTED | SUSPENDED",
  "publisherEmail": "string",
  "createdAt": "datetime"
}
```
- Show status badge on each website card
- Only APPROVED websites can have placements

### Placements — under websites

| Action | Method | Endpoint |
|---|---|---|
| List placements | GET | `/api/publisher/websites/{websiteId}/placements` |
| Create placement | POST | `/api/publisher/placements` |
| Toggle active | PUT | `/api/publisher/placements/{placementId}/toggle` |

**Placement Create Body:**
```json
{ "websiteId": 1, "campaignId": 1, "adCreativeId": 1 }
```
- `adCreativeId` is optional
- Campaign must be ACTIVE, website must be APPROVED

**Placement Response:**
```json
{
  "id": 1,
  "websiteId": 1,
  "websiteUrl": "string",
  "campaignId": 1,
  "campaignName": "string",
  "adCreativeId": 1,
  "active": true,
  "createdAt": "datetime"
}
```

### Earnings & Transactions
```
GET /api/publisher/earnings       → BigDecimal (total earned)
GET /api/publisher/transactions   → list of revenue transactions
```

**Revenue Transaction:**
```json
{
  "id": 1,
  "placementId": 1,
  "totalAmount": 0.10,
  "publisherShare": 0.0700,
  "networkShare": 0.0200,
  "platformShare": 0.0100,
  "createdAt": "datetime"
}
```

### Analytics
```
GET /api/publisher/websites/{id}/analytics?from=&to=
Response: { totalImpressions, totalClicks, ctr, totalRevenue }

GET /api/publisher/websites/{id}/analytics/daily?from=&to=
Response: [{ date, impressions, clicks }]
```

### Upgrade Request
```
POST /api/publisher/upgrade-requests
Body: { requestedRole: "NETWORK_ADMIN", reason: "string" }

GET /api/publisher/upgrade-requests
```

---

## NETWORK_ADMIN Panel

### Website Approvals
```
GET /api/network/websites/pending           → list pending websites
PUT /api/network/websites/{id}/approve      → approve website
PUT /api/network/websites/{id}/reject       → reject website
```

### Campaign Approvals
```
GET /api/network/campaigns/pending          → list pending campaigns
PUT /api/network/campaigns/{id}/approve     → approve (sets ACTIVE)
PUT /api/network/campaigns/{id}/reject      → reject campaign
```

---

## SUPER_ADMIN Panel

### Platform Stats Dashboard
```
GET /api/admin/stats
Response: {
  totalUsers, totalWebsites, totalCampaigns, totalPlacements,
  totalClicks, totalImpressions,
  totalPlatformRevenue, totalNetworkRevenue
}
```
- Show as stat cards + summary

### User Management
```
GET /api/admin/users                    → all users (no password exposed)
GET /api/admin/users/role/{role}        → filter by role
PUT /api/admin/users/{id}/suspend       → suspend user
PUT /api/admin/users/{id}/activate      → activate user
```

**User Response:**
```json
{
  "id": 1,
  "email": "string",
  "fullName": "string",
  "role": "SUPER_ADMIN | NETWORK_ADMIN | PUBLISHER | ADVERTISER",
  "status": "ACTIVE | INACTIVE | SUSPENDED",
  "createdAt": "datetime"
}
```

### Upgrade Request Review
```
GET /api/admin/upgrade-requests/pending     → list pending requests

PUT /api/admin/upgrade-requests/{id}/review
Body: { decision: "APPROVED | REJECTED", reviewNote: "string" }
```

**Upgrade Request Response:**
```json
{
  "id": 1,
  "userEmail": "string",
  "requestedRole": "PUBLISHER | NETWORK_ADMIN",
  "status": "PENDING | APPROVED | REJECTED",
  "reason": "string",
  "reviewNote": "string",
  "reviewedByEmail": "string",
  "createdAt": "datetime",
  "reviewedAt": "datetime"
}
```
- On APPROVED, the user's role is automatically updated in the backend

---

## Public Tracking Endpoints (no auth required)

```
POST /api/track/impression/{placementId}   → record an impression
POST /api/track/click/{placementId}        → record a click + split revenue
```
- These are pixel-style endpoints called when an ad is shown/clicked
- Click response returns the RevenueTransaction object

---

## Error Handling
All errors return:
```json
{ "error": "message" }
```
HTTP status codes:
- `400` — bad request / validation error
- `401` — unauthorized (missing/invalid token)
- `403` — forbidden (wrong role)
- `409` — conflict (e.g. duplicate email, already pending request)
- `500` — server error

Validation errors return a map:
```json
{ "fieldName": "error message" }
```

---

## UI Pages Needed

### Public
- `/login`
- `/register`

### Advertiser (`/advertiser/*`)
- Dashboard — spend summary, active campaigns count
- Campaigns list — with status badges and action buttons
- Campaign detail — creatives, placements, analytics chart
- Create/Edit campaign form

### Publisher (`/publisher/*`)
- Dashboard — earnings summary, websites count
- Websites list — with status badges
- Website detail — placements, analytics chart
- Register website form
- Placements management

### Network Admin (`/network/*`)
- Pending websites list — approve/reject
- Pending campaigns list — approve/reject

### Super Admin (`/admin/*`)
- Stats dashboard — cards + platform revenue
- Users list — with suspend/activate actions, role filter
- Upgrade requests — pending list with approve/reject

---

## Revenue Model (for display purposes)
Every click splits the CPC bid:
- Publisher: 70%
- Network Admin: 20%
- Platform: 10%

---

## Notes
- JWT is stored in `localStorage` as `token`
- Role is stored as `role` in `localStorage`
- All date params use format: `YYYY-MM-DD`
- All monetary values are `BigDecimal` (up to 4 decimal places)
- Swagger UI available at: `http://localhost:8080/swagger-ui/index.html`
