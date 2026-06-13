package com.pico.shared;

import com.pico.catalog.infrastructure.PlanRepository;
import com.pico.provisioning.domain.CloudResource;
import com.pico.provisioning.domain.ResourceStatus;
import com.pico.provisioning.domain.ResourceType;
import com.pico.provisioning.infrastructure.CloudResourceRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class SeedDataInitializer {

    private final CloudResourceRepository resourceRepository;
    private final PlanRepository planRepository;

    public SeedDataInitializer(CloudResourceRepository resourceRepository, PlanRepository planRepository) {
        this.resourceRepository = resourceRepository;
        this.planRepository = planRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    @Order(10)
    public void seed() {
        if (resourceRepository.count() > 0) return;

        var plans = planRepository.findAll();
        if (plans.isEmpty()) return;

        var starter = plans.stream().filter(p -> p.getName().contains("Starter")).findFirst().orElse(plans.get(0));
        var business = plans.stream().filter(p -> p.getName().contains("Business")).findFirst().orElse(plans.get(0));

        // Alice's resources
        resourceRepository.save(new CloudResource(
                UUID.fromString("aaaaaaaa-0001-0001-0001-000000000001"),
                "customer-alice", starter.getId(), "web-server-01",
                ResourceType.VM, ResourceStatus.RUNNING, "vm-a1b2c3d4", Instant.now().minusSeconds(86400)
        ));
        resourceRepository.save(new CloudResource(
                UUID.fromString("aaaaaaaa-0001-0001-0001-000000000002"),
                "customer-alice", business.getId(), "db-server-01",
                ResourceType.VM, ResourceStatus.STOPPED, "vm-e5f6a7b8", Instant.now().minusSeconds(43200)
        ));

        // Bob's resources
        resourceRepository.save(new CloudResource(
                UUID.fromString("bbbbbbbb-0002-0002-0002-000000000001"),
                "customer-bob", starter.getId(), "dev-vm-01",
                ResourceType.VM, ResourceStatus.RUNNING, "vm-c9d0e1f2", Instant.now().minusSeconds(3600)
        ));
    }
}
