package com.pico.usage.application;

import com.pico.usage.domain.UsageRecord;
import com.pico.usage.infrastructure.UsageRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pico.provisioning.domain.ResourceStatus;
import com.pico.provisioning.infrastructure.CloudResourceRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class UsageService {

    private final UsageRepository usageRepository;
    private final CloudResourceRepository resourceRepository;
    private final Random random = new Random();

    public UsageService(UsageRepository usageRepository, CloudResourceRepository resourceRepository) {
        this.usageRepository = usageRepository;
        this.resourceRepository = resourceRepository;
    }

    /** Simulate metering every 30s for all RUNNING resources */
    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void recordUsage() {
        resourceRepository.findByStatus(ResourceStatus.RUNNING).forEach(r -> {
            var cpuHours = BigDecimal.valueOf(0.5 + random.nextDouble() * 0.5).setScale(4, RoundingMode.HALF_UP);
            var storageGbHours = BigDecimal.valueOf(r.getPlanId().getLeastSignificantBits() % 200 + 50L)
                    .multiply(BigDecimal.valueOf(0.5)).setScale(4, RoundingMode.HALF_UP);
            usageRepository.save(new UsageRecord(UUID.randomUUID(), r.getId(),
                    cpuHours, storageGbHours.abs(), Instant.now(), Instant.now()));
        });
    }

    @Transactional(readOnly = true)
    public List<UsageRecord> getForResource(UUID resourceId) {
        return usageRepository.findByResourceIdOrderByRecordedAtDesc(resourceId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalCpuHours(UUID resourceId) {
        var result = usageRepository.sumCpuHoursByResourceId(resourceId);
        return result != null ? result : BigDecimal.ZERO;
    }
}
