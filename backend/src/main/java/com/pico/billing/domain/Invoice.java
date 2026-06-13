package com.pico.billing.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "invoices")
public class Invoice {
    @Id
    private UUID id;
    private String customerId;
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;
    @Column(name = "total_amount")
    private BigDecimal totalAmount;
    private Instant createdAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<InvoiceItem> items = new ArrayList<>();

    protected Invoice() {}

    public Invoice(UUID id, String customerId, InvoiceStatus status, BigDecimal totalAmount, Instant createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    public void markPaid() { this.status = InvoiceStatus.PAID; }
    public void issue() { this.status = InvoiceStatus.ISSUED; }
    public void cancel() { this.status = InvoiceStatus.CANCELLED; }
    public void addItem(InvoiceItem item) { items.add(item); }

    public UUID getId() { return id; }
    public String getCustomerId() { return customerId; }
    public InvoiceStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public Instant getCreatedAt() { return createdAt; }
    public List<InvoiceItem> getItems() { return items; }
}
