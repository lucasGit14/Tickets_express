package br.com.ticketsexpress.tickets_express_api.event.dto;

import br.com.ticketsexpress.tickets_express_api.event.EventStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EventResponse(
        UUID id,
        UUID organizerId,
        Long tmdbMovieId,
        String title,
        String posterUrl,
        String synopsis,
        Instant startsAt,
        String venue,
        String address,
        BigDecimal price,
        EventStatus status,
        Instant createdAt
) {
}
