package com.pico.billing.infrastructure;

import com.pico.billing.domain.Invoice;
import com.pico.billing.domain.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    List<Invoice> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    List<Invoice> findByStatus(InvoiceStatus status);
}
