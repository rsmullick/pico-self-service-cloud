package com.pico.catalog;

import com.pico.catalog.application.PlanService;
import com.pico.catalog.domain.Plan;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
class PlanServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("pico_test")
            .withUsername("pico")
            .withPassword("pico");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    PlanService planService;

    @Test
    void shouldReturnSeededPlans() {
        List<Plan> plans = planService.getAllPlans();
        assertThat(plans).isNotEmpty();
        assertThat(plans).allMatch(p -> p.getMonthlyPrice().compareTo(java.math.BigDecimal.ZERO) > 0);
    }

    @Test
    void shouldThrowWhenPlanNotFound() {
        assertThatThrownBy(() -> planService.getPlan(java.util.UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Plan not found");
    }

    @Test
    void shouldListPlansOrderedByPrice() {
        List<Plan> plans = planService.listPlans();
        for (int i = 0; i < plans.size() - 1; i++) {
            assertThat(plans.get(i).getMonthlyPrice())
                    .isLessThanOrEqualTo(plans.get(i + 1).getMonthlyPrice());
        }
    }
}
