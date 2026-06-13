package com.pico.catalog.api;

import com.pico.catalog.domain.Plan;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PlanResponse(
        UUID id,
        String name,
        String description,
        int cpu,
        int memoryGb,
        int storageGb,
        BigDecimal monthlyPrice,
        Instant createdAt
) {
    public static PlanResponse from(Plan p) {
        return new PlanResponse(p.getId(), p.getName(), p.getDescription(),
                p.getCpu(), p.getMemoryGb(), p.getStorageGb(), p.getMonthlyPrice(), p.getCreatedAt());
    }
}
