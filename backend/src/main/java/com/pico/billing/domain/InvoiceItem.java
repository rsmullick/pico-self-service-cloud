package com.pico.billing.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "invoice_items")
public class InvoiceItem {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    private String description;
    private BigDecimal amount;

    protected InvoiceItem() {}

    public InvoiceItem(UUID id, Invoice invoice, String description, BigDecimal amount) {
        this.id = id;
        this.invoice = invoice;
        this.description = description;
        this.amount = amount;
    }

    public UUID getId() { return id; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
}
