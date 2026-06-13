package com.pico.catalog.api;

import com.pico.catalog.application.PlanService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanService service;

    public PlanController(PlanService service) {
        this.service = service;
    }

    @GetMapping
    public List<PlanResponse> getPlans() {

        return service.getAllPlans()
                .stream()
                .map(PlanResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public PlanResponse getPlan(
            @PathVariable UUID id
    ) {
        return PlanResponse.from(
                service.getPlan(id)
        );
    }
}