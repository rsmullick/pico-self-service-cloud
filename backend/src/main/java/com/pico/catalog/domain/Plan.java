package com.pico.catalog.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "plans")
public class Plan {

    @Id
    private UUID id;

    private String name;

    private String description;

    private int cpu;

    private int memoryGb;

    private int storageGb;

    private BigDecimal monthlyPrice;

    private Instant createdAt;

    protected Plan() {
    }

    public Plan(
            UUID id,
            String name,
            String description,
            int cpu,
            int memoryGb,
            int storageGb,
            BigDecimal monthlyPrice,
            Instant createdAt
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cpu = cpu;
        this.memoryGb = memoryGb;
        this.storageGb = storageGb;
        this.monthlyPrice = monthlyPrice;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getCpu() {
        return cpu;
    }

    public int getMemoryGb() {
        return memoryGb;
    }

    public int getStorageGb() {
        return storageGb;
    }

    public BigDecimal getMonthlyPrice() {
        return monthlyPrice;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}