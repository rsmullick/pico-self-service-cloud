# PICO Self-Service Cloud — Architecture

## Module Map

The backend is structured as a Spring Modulith application. Each module owns its full vertical slice:

```
com.pico
├── auth          — In-memory user store, login endpoint
├── catalog       — VM plans, pricing, seed data
├── provisioning  — CloudResource entity, state machine, async provisioning simulation
├── billing       — Invoice lifecycle (DRAFT → ISSUED → PAID)
├── usage         — Scheduled metering simulation for RUNNING resources
├── audit         — Append-only audit log, written on every state change
└── shared        — CORS config, async/scheduling enablement, seed data initializer, error handler
```

## Key Design Decisions

### State Machine (provisioning)
`ResourceStateMachine` is a pure domain class with no Spring dependencies. It encodes valid transitions as a `switch` expression. Invalid transitions throw `IllegalStateException`, which the global error handler maps to HTTP 409. This makes the business rules easy to test in isolation.

### Async Provisioning
Provisioning is triggered via `@Async` after persisting the resource in `PENDING` state. The async task sleeps 3 seconds (simulating infrastructure API round-trip) then updates status to `RUNNING` (or `FAILED`). Each step records a `ResourceEvent` for the timeline view.

### Audit Trail
All state-changing operations call `AuditService.log()` in a separate transaction (`REQUIRES_NEW`) so audit records are never lost even if the outer transaction rolls back.

### Billing
Invoice generation is a snapshot: it collects all resources for a customer, looks up their plan prices, and creates an `Invoice` with `InvoiceItem` rows. This is intentionally simple; production billing would aggregate `usage_records` over billing periods.

### Auth
Authentication is mocked with an in-memory map. A production system would replace `UserStore` with Spring Security + JWT. The current design decouples auth concerns so this swap is straightforward.

## Frontend

Single-page React app using Vite. No UI framework — styles are implemented with CSS custom properties for a dark, modern aesthetic. API calls go through Vite's dev proxy to `/api/*` → `http://backend:8080`.

State management is local React state (useState/useEffect). Resources with pending/provisioning status trigger auto-polling every 3 seconds.

## Deployment

Docker Compose brings up postgres (with health check), backend (waits for postgres), and frontend (dev server with proxy). Flyway runs migrations on startup.
