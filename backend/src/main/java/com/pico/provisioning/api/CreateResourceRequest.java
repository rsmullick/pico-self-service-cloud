package com.pico.provisioning.api;

import java.util.UUID;

public record CreateResourceRequest(
        String customerId,
        UUID planId,
        String resourceName
) {
}