-- V1 initial schema for pico-self-service-cloud

-- ============================================================
-- PICO Self-Service Cloud
-- Initial Schema
-- ============================================================

-- ============================================================
-- Plans (Service Catalog)
-- ============================================================

CREATE TABLE IF NOT EXISTS plans (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    cpu INT NOT NULL,
    memory_gb INT NOT NULL,
    storage_gb INT NOT NULL,
    monthly_price NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- Cloud Resources
-- ============================================================

CREATE TABLE IF NOT EXISTS cloud_resources (
    id UUID PRIMARY KEY,
    customer_id VARCHAR(255) NOT NULL,
    plan_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,

    resource_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,

    external_resource_id VARCHAR(255),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cloud_resource_plan
        FOREIGN KEY (plan_id)
        REFERENCES plans(id)
);

CREATE INDEX idx_cloud_resources_customer
    ON cloud_resources(customer_id);

CREATE INDEX idx_cloud_resources_status
    ON cloud_resources(status);

-- ============================================================
-- Resource Timeline / Events
-- ============================================================

CREATE TABLE IF NOT EXISTS resource_events (
    id UUID PRIMARY KEY,

    resource_id UUID NOT NULL,

    event_type VARCHAR(100) NOT NULL,

    details TEXT,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_resource_event_resource
        FOREIGN KEY (resource_id)
        REFERENCES cloud_resources(id)
);

CREATE INDEX idx_resource_events_resource
    ON resource_events(resource_id);

-- ============================================================
-- Usage Metering
-- ============================================================

CREATE TABLE IF NOT EXISTS usage_records (
    id UUID PRIMARY KEY,

    resource_id UUID NOT NULL,

    cpu_hours NUMERIC(20,4) NOT NULL DEFAULT 0,

    storage_gb_hours NUMERIC(20,4) NOT NULL DEFAULT 0,

    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_usage_resource
        FOREIGN KEY (resource_id)
        REFERENCES cloud_resources(id)
);

CREATE INDEX idx_usage_resource
    ON usage_records(resource_id);

-- ============================================================
-- Billing
-- ============================================================

CREATE TABLE IF NOT EXISTS invoices (
    id UUID PRIMARY KEY,

    customer_id VARCHAR(255) NOT NULL,

    status VARCHAR(50) NOT NULL,

    total_amount NUMERIC(10,2) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_invoices_customer
    ON invoices(customer_id);

CREATE TABLE IF NOT EXISTS invoice_items (
    id UUID PRIMARY KEY,

    invoice_id UUID NOT NULL,

    description VARCHAR(255) NOT NULL,

    amount NUMERIC(10,2) NOT NULL,

    CONSTRAINT fk_invoice_item_invoice
        FOREIGN KEY (invoice_id)
        REFERENCES invoices(id)
        ON DELETE CASCADE
);

-- ============================================================
-- Audit Log
-- ============================================================

CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY,

    event_type VARCHAR(100) NOT NULL,

    entity_type VARCHAR(100) NOT NULL,

    entity_id VARCHAR(255) NOT NULL,

    actor_id VARCHAR(255),

    changes JSONB,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_entity
    ON audit_logs(entity_type, entity_id);


