package com.pico.catalog.application;

import com.pico.catalog.domain.Plan;
import com.pico.catalog.infrastructure.PlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PlanService {

    private final PlanRepository repository;

    public PlanService(PlanRepository repository) {
        this.repository = repository;
    }

    public List<Plan> getAllPlans() {
        return repository.findAll();
    }

    public List<Plan> listPlans() {
        return repository.findAllByOrderByMonthlyPriceAsc();
    }

    public Plan getPlan(UUID id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Plan not found: " + id
                        ));
    }
}