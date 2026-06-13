package com.pico.billing.application;

import com.pico.audit.application.AuditService;
import com.pico.billing.domain.Invoice;
import com.pico.billing.domain.InvoiceItem;
import com.pico.billing.domain.InvoiceStatus;
import com.pico.billing.infrastructure.InvoiceRepository;
import com.pico.catalog.infrastructure.PlanRepository;
import com.pico.provisioning.infrastructure.CloudResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class BillingService {

    private final InvoiceRepository invoiceRepository;
    private final CloudResourceRepository resourceRepository;
    private final PlanRepository planRepository;
    private final AuditService auditService;

    public BillingService(InvoiceRepository invoiceRepository,
                          CloudResourceRepository resourceRepository,
                          PlanRepository planRepository,
                          AuditService auditService) {
        this.invoiceRepository = invoiceRepository;
        this.resourceRepository = resourceRepository;
        this.planRepository = planRepository;
        this.auditService = auditService;
    }

    @Transactional
    public Invoice generateInvoice(String customerId, String actorId) {
        var resources = resourceRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        if (resources.isEmpty()) {
            throw new IllegalStateException("No resources found for customer: " + customerId);
        }

        var invoice = new Invoice(UUID.randomUUID(), customerId, InvoiceStatus.ISSUED, BigDecimal.ZERO, Instant.now());

        BigDecimal total = BigDecimal.ZERO;
        for (var resource : resources) {
            var plan = planRepository.findById(resource.getPlanId()).orElse(null);
            if (plan == null) continue;
            invoice.addItem(new InvoiceItem(UUID.randomUUID(), invoice,
                    resource.getName() + " (" + plan.getName() + ")", plan.getMonthlyPrice()));
            total = total.add(plan.getMonthlyPrice());
        }

        // Create new invoice with correct total (Invoice is immutable except for status/items)
        var finalInvoice = new Invoice(invoice.getId(), customerId, InvoiceStatus.ISSUED, total, invoice.getCreatedAt());
        for (var item : invoice.getItems()) {
            finalInvoice.addItem(new InvoiceItem(UUID.randomUUID(), finalInvoice, item.getDescription(), item.getAmount()));
        }

        invoiceRepository.save(finalInvoice);
        auditService.log("INVOICE_GENERATED", "invoice", finalInvoice.getId().toString(), actorId, null);
        return finalInvoice;
    }

    @Transactional
    public Invoice markPaid(UUID invoiceId, String actorId) {
        var invoice = getById(invoiceId);
        if (invoice.getStatus() != InvoiceStatus.ISSUED) {
            throw new IllegalStateException("Only ISSUED invoices can be marked as paid");
        }
        invoice.markPaid();
        invoiceRepository.save(invoice);
        auditService.log("INVOICE_PAID", "invoice", invoiceId.toString(), actorId, null);
        return invoice;
    }

    @Transactional(readOnly = true)
    public List<Invoice> listForCustomer(String customerId) {
        return invoiceRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Transactional(readOnly = true)
    public Invoice getById(UUID id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + id));
    }
}
