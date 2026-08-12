package br.com.ticketsexpress.tickets_express_api.ticket;

import br.com.ticketsexpress.tickets_express_api.auth.ApplicationUser;
import br.com.ticketsexpress.tickets_express_api.event.Seat;
import br.com.ticketsexpress.tickets_express_api.reservation.Reservation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false, unique = true)
    private Seat seat;

    @Column(name = "code_raw", nullable = false, unique = true, length = 64)
    private String codeRaw;

    @Column(name = "code_hash", nullable = false, unique = true, length = 64)
    private String codeHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private ApplicationUser owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    @Column(name = "validated_at")
    private Instant validatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validated_by")
    private ApplicationUser validatedBy;

    @Column(name = "share_token_hash", unique = true, length = 64)
    private String shareTokenHash;

    @Column(name = "share_expires_at")
    private Instant shareExpiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Ticket() {
    }

    public Ticket(UUID id,
                  Reservation reservation,
                  Seat seat,
                  String codeRaw,
                  String codeHash,
                  ApplicationUser owner,
                  TicketStatus status,
                  Instant validatedAt,
                  ApplicationUser validatedBy,
                  String shareTokenHash,
                  Instant shareExpiresAt,
                  Instant createdAt) {
        this.id = id;
        this.reservation = reservation;
        this.seat = seat;
        this.codeRaw = codeRaw;
        this.codeHash = codeHash;
        this.owner = owner;
        this.status = status;
        this.validatedAt = validatedAt;
        this.validatedBy = validatedBy;
        this.shareTokenHash = shareTokenHash;
        this.shareExpiresAt = shareExpiresAt;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Seat getSeat() {
        return seat;
    }

    public String getCodeRaw() {
        return codeRaw;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public ApplicationUser getOwner() {
        return owner;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public Instant getValidatedAt() {
        return validatedAt;
    }

    public ApplicationUser getValidatedBy() {
        return validatedBy;
    }

    public String getShareTokenHash() {
        return shareTokenHash;
    }

    public Instant getShareExpiresAt() {
        return shareExpiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
