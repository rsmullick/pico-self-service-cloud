package com.pico.provisioning.api;

import com.pico.provisioning.application.ProvisioningService;
import com.pico.provisioning.application.ResourceEventRepository;
import com.pico.provisioning.domain.ResourceAction;
import com.pico.provisioning.domain.ResourceEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ProvisioningService provisioningService;
    private final ResourceEventRepository eventRepository;

    public ResourceController(ProvisioningService provisioningService, ResourceEventRepository eventRepository) {
        this.provisioningService = provisioningService;
        this.eventRepository = eventRepository;
    }

    @PostMapping
    public ResponseEntity<ResourceResponse> provision(@RequestBody CreateResourceRequest req) {
        var resource = provisioningService.provision(req.customerId(), req.planId(), req.resourceName());
        return ResponseEntity.ok(ResourceResponse.from(resource));
    }

    @GetMapping
    public List<ResourceResponse> list(@RequestParam String customerId) {
        return provisioningService.listForCustomer(customerId)
                .stream().map(ResourceResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ResourceResponse get(@PathVariable UUID id) {
        return ResourceResponse.from(provisioningService.getById(id));
    }

    @PostMapping("/{id}/actions")
    public ResponseEntity<ResourceResponse> action(
            @PathVariable UUID id,
            @RequestBody ResourceActionRequest req
    ) {
        var action = ResourceAction.valueOf(req.action().toUpperCase());
        var updated = provisioningService.performAction(id, action, req.actorId());
        return ResponseEntity.ok(ResourceResponse.from(updated));
    }

    @GetMapping("/{id}/events")
    public List<ResourceEvent> events(@PathVariable UUID id) {
        return eventRepository.findByResourceIdOrderByCreatedAtAsc(id);
    }
}
