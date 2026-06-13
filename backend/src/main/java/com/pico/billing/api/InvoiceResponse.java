package com.pico.billing.api;

import com.pico.billing.domain.Invoice;
import com.pico.billing.domain.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        String customerId,
        InvoiceStatus status,
        BigDecimal totalAmount,
        Instant createdAt,
        List<InvoiceItemResponse> items
) {
    public record InvoiceItemResponse(UUID id, String description, BigDecimal amount) {}

    public static InvoiceResponse from(Invoice invoice) {
        var items = invoice.getItems().stream()
                .map(i -> new InvoiceItemResponse(i.getId(), i.getDescription(), i.getAmount()))
                .toList();
        return new InvoiceResponse(invoice.getId(), invoice.getCustomerId(),
                invoice.getStatus(), invoice.getTotalAmount(), invoice.getCreatedAt(), items);
    }
}
