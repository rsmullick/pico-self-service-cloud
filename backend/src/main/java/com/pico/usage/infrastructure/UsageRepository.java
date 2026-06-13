package com.pico.usage.infrastructure;

import com.pico.usage.domain.UsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UsageRepository extends JpaRepository<UsageRecord, UUID> {
    List<UsageRecord> findByResourceIdOrderByRecordedAtDesc(UUID resourceId);

    @Query("SELECT SUM(u.cpuHours) FROM UsageRecord u WHERE u.resourceId = :resourceId")
    java.math.BigDecimal sumCpuHoursByResourceId(UUID resourceId);
}
