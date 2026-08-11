package br.com.ticketsexpress.tickets_express_api.ticket;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    Optional<Ticket> findByCodeHash(String codeHash);
    Optional<Ticket> findByShareTokenHash(String shareTokenHash);
    List<Ticket> findByReservationCustomerIdOrderByCreatedAtDesc(UUID customerId);
}
