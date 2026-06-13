package com.pico.provisioning.application;

import com.pico.audit.application.AuditService;
import com.pico.catalog.infrastructure.PlanRepository;
import com.pico.provisioning.domain.*;
import com.pico.provisioning.infrastructure.CloudResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ProvisioningService {

    private static final Logger log = LoggerFactory.getLogger(ProvisioningService.class);

    private final CloudResourceRepository resourceRepository;
    private final PlanRepository planRepository;
    private final ResourceStateMachine stateMachine;
    private final AuditService auditService;
    private final ResourceEventRepository eventRepository;

    public ProvisioningService(
            CloudResourceRepository resourceRepository,
            PlanRepository planRepository,
            AuditService auditService,
            ResourceEventRepository eventRepository
    ) {
        this.resourceRepository = resourceRepository;
        this.planRepository = planRepository;
        this.stateMachine = new ResourceStateMachine();
        this.auditService = auditService;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public CloudResource provision(String customerId, UUID planId, String name) {
        planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));

        var resource = new CloudResource(
                UUID.randomUUID(), customerId, planId, name,
                ResourceType.VM, ResourceStatus.PENDING, null, Instant.now()
        );
        resourceRepository.save(resource);
        auditService.log("RESOURCE_CREATED", "cloud_resource", resource.getId().toString(), customerId, null);
        saveEvent(resource.getId(), "CREATED", "Resource created with plan " + planId);

        simulateProvisioning(resource.getId(), customerId);
        return resource;
    }

    @Async
    public void simulateProvisioning(UUID resourceId, String actorId) {
        try {
            doUpdateStatus(resourceId, ResourceAction.START_PROVISIONING, actorId, "Provisioning started");
            Thread.sleep(3000);
            String externalId = "vm-" + UUID.randomUUID().toString().substring(0, 8);
            doUpdateStatusWithExternal(resourceId, ResourceAction.PROVISION_SUCCESS, actorId, "VM online", externalId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            doUpdateStatus(resourceId, ResourceAction.PROVISION_FAILED, actorId, "Interrupted");
        } catch (Exception e) {
            log.error("Provisioning failed for {}", resourceId, e);
            doUpdateStatus(resourceId, ResourceAction.PROVISION_FAILED, actorId, "Error: " + e.getMessage());
        }
    }

    @Transactional
    public CloudResource performAction(UUID resourceId, ResourceAction action, String actorId) {
        var resource = getById(resourceId);
        var newStatus = stateMachine.transition(resource.getStatus(), action);
        var updated = new CloudResource(resource.getId(), resource.getCustomerId(), resource.getPlanId(),
                resource.getName(), resource.getResourceType(), newStatus,
                resource.getExternalResourceId(), resource.getCreatedAt());
        resourceRepository.save(updated);
        auditService.log("RESOURCE_" + action.name(), "cloud_resource", resourceId.toString(), actorId, null);
        saveEvent(resourceId, action.name(), "Status changed to " + newStatus);
        return updated;
    }

    @Transactional(readOnly = true)
    public List<CloudResource> listForCustomer(String customerId) {
        return resourceRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Transactional(readOnly = true)
    public CloudResource getById(UUID id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + id));
    }

    private void saveEvent(UUID resourceId, String type, String details) {
        eventRepository.save(new ResourceEvent(UUID.randomUUID(), resourceId, type, details, Instant.now()));
    }

    private void doUpdateStatus(UUID resourceId, ResourceAction action, String actorId, String detail) {
        try {
            performAction(resourceId, action, actorId);
        } catch (Exception e) {
            log.error("Failed status update {} for {}: {}", action, resourceId, e.getMessage());
        }
    }

    private void doUpdateStatusWithExternal(UUID resourceId, ResourceAction action, String actorId, String detail, String externalId) {
        try {
            var resource = resourceRepository.findById(resourceId).orElseThrow();
            var newStatus = stateMachine.transition(resource.getStatus(), action);
            var updated = new CloudResource(resource.getId(), resource.getCustomerId(), resource.getPlanId(),
                    resource.getName(), resource.getResourceType(), newStatus,
                    externalId, resource.getCreatedAt());
            resourceRepository.save(updated);
            auditService.log("RESOURCE_" + action.name(), "cloud_resource", resourceId.toString(), actorId, null);
            saveEvent(resourceId, action.name(), detail + " | id=" + externalId);
        } catch (Exception e) {
            log.error("Failed external status update for {}: {}", resourceId, e.getMessage());
        }
    }
}
