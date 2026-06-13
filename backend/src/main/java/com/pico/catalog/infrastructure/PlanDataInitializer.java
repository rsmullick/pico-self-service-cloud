package com.pico.catalog.infrastructure;

import com.pico.catalog.domain.Plan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
public class PlanDataInitializer {

    private final PlanRepository repository;

    public PlanDataInitializer(
            PlanRepository repository
    ) {
        this.repository = repository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {

        if (repository.count() > 0) {
            return;
        }

        repository.save(
                new Plan(
                        UUID.randomUUID(),
                        "Starter VM",
                        "Small development instance",
                        2,
                        4,
                        50,
                        new BigDecimal("15.00"),
                        Instant.now()
                )
        );

        repository.save(
                new Plan(
                        UUID.randomUUID(),
                        "Business VM",
                        "General business workload",
                        4,
                        8,
                        100,
                        new BigDecimal("35.00"),
                        Instant.now()
                )
        );

        repository.save(
                new Plan(
                        UUID.randomUUID(),
                        "Enterprise VM",
                        "Large production workload",
                        8,
                        16,
                        200,
                        new BigDecimal("75.00"),
                        Instant.now()
                )
        );
    }
}