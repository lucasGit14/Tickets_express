package br.com.ticketsexpress.tickets_express_api.reservation;

import br.com.ticketsexpress.tickets_express_api.auth.ApplicationUser;
import br.com.ticketsexpress.tickets_express_api.event.Event;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private ApplicationUser customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "payment_reference", length = 80)
    private String paymentReference;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Reservation() {
    }

    public Reservation(UUID id, ApplicationUser customer, Event event, ReservationStatus status, Instant expiresAt, BigDecimal totalAmount, String paymentReference, Instant createdAt) {
        this.id = id;
        this.customer = customer;
        this.event = event;
        this.status = status;
        this.expiresAt = expiresAt;
        this.totalAmount = totalAmount;
        this.paymentReference = paymentReference;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public ApplicationUser getCustomer() {
        return customer;
    }

    public Event getEvent() {
        return event;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
