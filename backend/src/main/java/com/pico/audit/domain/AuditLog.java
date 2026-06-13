package com.pico.audit.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    private UUID id;
    private String eventType;
    private String entityType;
    private String entityId;
    private String actorId;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String changes;
    private Instant createdAt;

    protected AuditLog() {}

    public AuditLog(UUID id, String eventType, String entityType, String entityId,
                    String actorId, String changes, Instant createdAt) {
        this.id = id;
        this.eventType = eventType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.actorId = actorId;
        this.changes = changes;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getEventType() { return eventType; }
    public String getEntityType() { return entityType; }
    public String getEntityId() { return entityId; }
    public String getActorId() { return actorId; }
    public String getChanges() { return changes; }
    public Instant getCreatedAt() { return createdAt; }
}
