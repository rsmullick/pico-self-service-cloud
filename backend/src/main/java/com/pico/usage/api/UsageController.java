package com.pico.usage.api;

import com.pico.usage.application.UsageService;
import com.pico.usage.domain.UsageRecord;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usage")
public class UsageController {

    private final UsageService usageService;

    public UsageController(UsageService usageService) {
        this.usageService = usageService;
    }

    @GetMapping("/{resourceId}")
    public List<UsageRecord> getUsage(@PathVariable UUID resourceId) {
        return usageService.getForResource(resourceId);
    }

    @GetMapping("/{resourceId}/total-cpu-hours")
    public BigDecimal totalCpuHours(@PathVariable UUID resourceId) {
        return usageService.getTotalCpuHours(resourceId);
    }
}
