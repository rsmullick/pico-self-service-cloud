package com.pico.provisioning.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cloud_resources")
public class CloudResource {

    @Id
    private UUID id;
    private String customerId;
    private UUID planId;
    private String name;

    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    @Enumerated(EnumType.STRING)
    private ResourceStatus status;

    @Column(name = "external_resource_id")
    private String externalResourceId;

    private Instant createdAt;

    protected CloudResource() {}

    public CloudResource(UUID id, String customerId, UUID planId, String name,
                         ResourceType resourceType, ResourceStatus status,
                         String externalResourceId, Instant createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.planId = planId;
        this.name = name;
        this.resourceType = resourceType;
        this.status = status;
        this.externalResourceId = externalResourceId;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getCustomerId() { return customerId; }
    public UUID getPlanId() { return planId; }
    public String getName() { return name; }
    public ResourceType getResourceType() { return resourceType; }
    public ResourceStatus getStatus() { return status; }
    public String getExternalResourceId() { return externalResourceId; }
    public Instant getCreatedAt() { return createdAt; }
}
