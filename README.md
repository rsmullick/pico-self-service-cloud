# PICO Self-Service Cloud

**Option 2: PICO Self-Service Cloud Module**

A customer-facing self-service cloud portal where users can browse VM plans, provision infrastructure, manage their resources, and handle billing — all without manual support.

---

## Quick Start

```bash
git clone <your-repo-url>
cd pico-self-service-cloud
docker compose up --build
```

- **Frontend:** http://localhost:5173
- **Backend API:** http://localhost:8080
- **Health check:** http://localhost:8080/actuator/health

---

## Demo Credentials

| User | Email | Password | Role |
|------|-------|----------|------|
| Alice Chen | alice@pico.io | demo1234 | USER |
| Bob Tanaka | bob@pico.io | demo1234 | USER |
| PICO Admin | admin@pico.io | admin1234 | ADMIN |

Seed data includes pre-provisioned VMs for Alice and Bob so reviewers can immediately see the full interface without going through provisioning themselves.

---

## Key User Flows

### 1. Sign In
- Navigate to http://localhost:5173
- Use the demo quick-access buttons or enter credentials manually

### 2. Browse Catalog & Provision a VM
- Click **Catalog** in the nav
- Select a plan (Starter $15/mo, Business $35/mo, Enterprise $75/mo)
- Enter a resource name and click **Provision VM**
- Watch the resource appear in the Resources tab, transitioning `PENDING → PROVISIONING → RUNNING` (takes ~3 seconds, auto-refreshes)

### 3. Manage Resources
- Click **Resources** to see all VMs with status badges
- Click any resource to open the detail view
- Perform actions: **Stop**, **Start**, **Terminate** (state machine enforced)
- View the full event timeline per resource

### 4. Generate & Pay Invoices
- Click **Billing**
- Click **Generate Invoice** — itemizes all active resources with plan costs
- Expand an invoice to see line items
- Click **Mark Paid** to settle an issued invoice

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│  React Frontend (Vite + TypeScript)  :5173              │
│  - Login  - Catalog  - Resources  - Billing             │
└──────────────────┬──────────────────────────────────────┘
                   │  HTTP /api/* (proxied by Vite)
┌──────────────────▼──────────────────────────────────────┐
│  Spring Boot Backend  :8080                             │
│  ┌──────────┐ ┌──────────────┐ ┌────────┐ ┌─────────┐  │
│  │ catalog  │ │ provisioning │ │billing │ │  usage  │  │
│  └──────────┘ └──────────────┘ └────────┘ └─────────┘  │
│  ┌──────────┐ ┌──────────────┐                          │
│  │  audit   │ │    auth      │                          │
│  └──────────┘ └──────────────┘                          │
└──────────────────┬──────────────────────────────────────┘
                   │  JPA / Flyway
┌──────────────────▼──────────────────────────────────────┐
│  PostgreSQL 16  :5432  (db: pico)                       │
└─────────────────────────────────────────────────────────┘
```

The backend uses **Spring Modulith** to enforce module boundaries. Each module owns its domain, application service, infrastructure (repository), and API layer.

---

## Data Model Overview

| Table | Purpose |
|-------|---------|
| `plans` | Service catalog — VM tiers with pricing |
| `cloud_resources` | Provisioned VMs per customer with status |
| `resource_events` | Per-resource timeline (provisioning steps, actions) |
| `invoices` | Customer invoices with status lifecycle |
| `invoice_items` | Line items linked to each invoice |
| `usage_records` | Simulated CPU/storage metering (recorded every 30s for RUNNING VMs) |
| `audit_logs` | Immutable audit trail with actor, entity, event type |

---

## Provisioning State Machine

```
PENDING → PROVISIONING → RUNNING ⇄ STOPPED
                     ↓               ↓
                   FAILED        TERMINATED
```

The `ResourceStateMachine` class enforces valid transitions. Invalid actions throw `IllegalStateException`, returned as HTTP 409.

Provisioning is simulated asynchronously (3-second delay) using Spring's `@Async` with virtual threads.

---

## API Reference (abbreviated)

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/auth/login | Authenticate user |
| GET | /api/plans | List VM plans |
| POST | /api/resources | Provision a VM |
| GET | /api/resources?customerId={id} | List resources |
| POST | /api/resources/{id}/actions | Perform stop/start/terminate |
| GET | /api/resources/{id}/events | Resource event timeline |
| POST | /api/invoices/generate | Generate invoice for customer |
| POST | /api/invoices/{id}/pay | Mark invoice paid |
| GET | /api/audit | Recent audit log entries |

---

## Known Limitations

- Authentication is mocked (in-memory user store, no JWT). A production system would use Spring Security with JWT or OAuth2.
- "Billing" generates a one-time snapshot; real billing would require monthly scheduling and period-based aggregation.
- Usage metering is simulated with random values; a real system would hook into the compute API.
- No real infrastructure is provisioned — `externalResourceId` is a randomly generated mock handle.
- No email notifications — a production system would publish events via Kafka/RabbitMQ.

---

## What I Would Improve With More Time

1. **Real JWT auth** with role-based access control (RBAC) at the API level
2. **Scheduled billing** — monthly invoice generation via a cron job
3. **Real usage aggregation** — usage_records → invoice line items with per-resource cost breakdown
4. **Pagination** on resource/invoice lists
5. **WebSocket or SSE** for live provisioning status instead of polling
6. **Admin dashboard** — cross-customer view for the ADMIN role
7. **Integration tests** using Testcontainers with a real PostgreSQL instance
8. **OpenAPI/Swagger** documentation

---

## Tech Stack

- **Backend:** Java 21, Spring Boot 3.5, Spring Modulith, Spring Data JPA, Flyway, Virtual Threads
- **Frontend:** React 18, TypeScript, Vite (no UI framework — vanilla CSS custom properties)
- **Database:** PostgreSQL 16
- **Infrastructure:** Docker Compose
- **CI:** GitHub Actions (backend build, frontend build, docker-compose validation)

---

## AI Tools Used

Claude (claude.ai) was used to accelerate implementation of the service layer, repository interfaces, React UI components, and this README. All generated code was reviewed for correctness, architectural consistency with the existing module structure, and alignment with the assignment requirements. The domain model, state machine, database schema, and module organization were designed manually before AI assistance was engaged.
