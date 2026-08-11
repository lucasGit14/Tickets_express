package br.com.ticketsexpress.tickets_express_api.event.dto;

import br.com.ticketsexpress.tickets_express_api.event.SeatCategory;

import java.util.UUID;

public record SeatResponse(
        UUID id,
        UUID eventId,
        String rowLabel,
        Integer seatNumber,
        SeatCategory category
) {
}
