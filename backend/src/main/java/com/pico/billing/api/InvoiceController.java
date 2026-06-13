package com.pico.billing.api;

import com.pico.billing.application.BillingService;
import com.pico.billing.domain.Invoice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final BillingService billingService;

    public InvoiceController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping("/generate")
    public ResponseEntity<InvoiceResponse> generate(@RequestBody GenerateInvoiceRequest req) {
        var invoice = billingService.generateInvoice(req.customerId(), req.actorId());
        return ResponseEntity.ok(InvoiceResponse.from(invoice));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<InvoiceResponse> pay(@PathVariable UUID id, @RequestBody PayInvoiceRequest req) {
        var invoice = billingService.markPaid(id, req.actorId());
        return ResponseEntity.ok(InvoiceResponse.from(invoice));
    }

    @GetMapping
    public List<InvoiceResponse> list(@RequestParam String customerId) {
        return billingService.listForCustomer(customerId).stream().map(InvoiceResponse::from).toList();
    }

    @GetMapping("/{id}")
    public InvoiceResponse get(@PathVariable UUID id) {
        return InvoiceResponse.from(billingService.getById(id));
    }
}
