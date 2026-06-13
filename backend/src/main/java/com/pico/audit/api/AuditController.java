package com.pico.audit.api;

import com.pico.audit.application.AuditService;
import com.pico.audit.domain.AuditLog;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public List<AuditLog> recent() {
        return auditService.getRecent();
    }

    @GetMapping("/{entityType}/{entityId}")
    public List<AuditLog> forEntity(
            @PathVariable String entityType,
            @PathVariable String entityId) {
        return auditService.getForEntity(entityType, entityId);
    }
}
