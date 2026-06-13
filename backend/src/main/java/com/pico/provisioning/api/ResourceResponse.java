package com.pico.provisioning.api;

import com.pico.provisioning.domain.CloudResource;
import com.pico.provisioning.domain.ResourceStatus;
import com.pico.provisioning.domain.ResourceType;

import java.time.Instant;
import java.util.UUID;

public record ResourceResponse(
        UUID id,
        String customerId,
        String name,
        ResourceStatus status,
        ResourceType resourceType,
        UUID planId,
        String externalResourceId,
        Instant createdAt
) {
    public static ResourceResponse from(CloudResource r) {
        return new ResourceResponse(r.getId(), r.getCustomerId(), r.getName(),
                r.getStatus(), r.getResourceType(), r.getPlanId(),
                r.getExternalResourceId(), r.getCreatedAt());
    }
}
