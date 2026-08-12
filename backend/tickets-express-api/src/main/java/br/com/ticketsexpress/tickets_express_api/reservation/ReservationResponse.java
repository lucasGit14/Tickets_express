package br.com.ticketsexpress.tickets_express_api.reservation;

import br.com.ticketsexpress.tickets_express_api.ticket.TicketResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        UUID eventId,
        String eventTitle,
        UUID customerId,
        String customerName,
        String customerEmail,
        ReservationStatus status,
        BigDecimal totalAmount,
        Instant expiresAt,
        Instant createdAt,
        String paymentReference,
        List<UUID> seatIds,
        List<TicketResponse> tickets
) {
}
