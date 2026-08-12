package br.com.ticketsexpress.tickets_express_api.ticket;

import br.com.ticketsexpress.tickets_express_api.event.SeatCategory;

import java.time.Instant;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        String code,
        UUID reservationId,
        UUID eventId,
        String eventTitle,
        UUID seatId,
        String rowLabel,
        Integer seatNumber,
        SeatCategory seatCategory,
        UUID ownerId,
        String ownerName,
        String ownerEmail,
        TicketStatus status,
        Instant createdAt,
        Instant usedAt
) {
}
