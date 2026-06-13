package com.pico.audit.application;

import com.pico.audit.domain.AuditLog;
import com.pico.audit.infrastructure.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String eventType, String entityType, String entityId, String actorId, String changes) {
        repository.save(new AuditLog(
                UUID.randomUUID(), eventType, entityType, entityId, actorId, changes, Instant.now()
        ));
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getForEntity(String entityType, String entityId) {
        return repository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getRecent() {
        return repository.findTop50ByOrderByCreatedAtDesc();
    }
}
