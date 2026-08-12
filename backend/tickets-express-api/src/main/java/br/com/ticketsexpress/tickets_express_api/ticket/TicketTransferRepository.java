package br.com.ticketsexpress.tickets_express_api.ticket;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TicketTransferRepository extends JpaRepository<TicketTransfer, UUID> {
}
