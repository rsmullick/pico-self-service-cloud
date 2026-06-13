package com.pico.usage.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usage_records")
public class UsageRecord {
    @Id
    private UUID id;
    private UUID resourceId;
    private BigDecimal cpuHours;
    private BigDecimal storageGbHours;
    private Instant recordedAt;
    private Instant createdAt;

    protected UsageRecord() {}

    public UsageRecord(UUID id, UUID resourceId, BigDecimal cpuHours, BigDecimal storageGbHours,
                       Instant recordedAt, Instant createdAt) {
        this.id = id;
        this.resourceId = resourceId;
        this.cpuHours = cpuHours;
        this.storageGbHours = storageGbHours;
        this.recordedAt = recordedAt;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getResourceId() { return resourceId; }
    public BigDecimal getCpuHours() { return cpuHours; }
    public BigDecimal getStorageGbHours() { return storageGbHours; }
    public Instant getRecordedAt() { return recordedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
