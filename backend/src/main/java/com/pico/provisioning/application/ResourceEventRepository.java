package com.pico.provisioning.application;

import com.pico.provisioning.domain.ResourceEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ResourceEventRepository extends JpaRepository<ResourceEvent, UUID> {
    List<ResourceEvent> findByResourceIdOrderByCreatedAtAsc(UUID resourceId);
}
