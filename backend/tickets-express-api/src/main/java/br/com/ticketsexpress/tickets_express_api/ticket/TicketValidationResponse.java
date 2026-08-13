package br.com.ticketsexpress.tickets_express_api.ticket;

import java.time.Instant;
import java.util.UUID;

public record TicketValidationResponse(
        TicketValidationStatus status,
        String message,
        UUID ticketId,
        String code,
        UUID eventId,
        String eventTitle,
        String ownerName,
        Instant validatedAt
) {
}
