package com.pico.provisioning.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "resource_events")
public class ResourceEvent {

    @Id
    private UUID id;

    private UUID resourceId;

    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String details;

    private Instant createdAt;

    protected ResourceEvent() {}

    public ResourceEvent(UUID id, UUID resourceId, String eventType, String details, Instant createdAt) {
        this.id = id;
        this.resourceId = resourceId;
        this.eventType = eventType;
        this.details = details;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getResourceId() { return resourceId; }
    public String getEventType() { return eventType; }
    public String getDetails() { return details; }
    public Instant getCreatedAt() { return createdAt; }
}
