package com.pico.provisioning.infrastructure;

import com.pico.provisioning.domain.CloudResource;
import com.pico.provisioning.domain.ResourceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CloudResourceRepository extends JpaRepository<CloudResource, UUID> {
    List<CloudResource> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    List<CloudResource> findByStatus(ResourceStatus status);
}
