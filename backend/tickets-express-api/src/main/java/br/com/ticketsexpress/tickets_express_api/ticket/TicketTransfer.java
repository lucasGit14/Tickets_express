package br.com.ticketsexpress.tickets_express_api.ticket;

import br.com.ticketsexpress.tickets_express_api.auth.ApplicationUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ticket_transfers")
public class TicketTransfer {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    private ApplicationUser fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    private ApplicationUser toUser;

    @Column(name = "transferred_at", nullable = false)
    private Instant transferredAt;

    public TicketTransfer() {
    }

    public TicketTransfer(UUID id, Ticket ticket, ApplicationUser fromUser, ApplicationUser toUser, Instant transferredAt) {
        this.id = id;
        this.ticket = ticket;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.transferredAt = transferredAt;
    }

    public UUID getId() {
        return id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public ApplicationUser getFromUser() {
        return fromUser;
    }

    public ApplicationUser getToUser() {
        return toUser;
    }

    public Instant getTransferredAt() {
        return transferredAt;
    }
}
