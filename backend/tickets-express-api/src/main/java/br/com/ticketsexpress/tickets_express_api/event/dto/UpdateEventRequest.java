package br.com.ticketsexpress.tickets_express_api.event.dto;

import br.com.ticketsexpress.tickets_express_api.event.EventStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record UpdateEventRequest(
        Long tmdbMovieId,
        String title,
        String posterUrl,
        String synopsis,
        Instant startsAt,
        String venue,
        String address,
        BigDecimal price,
        EventStatus status
) {
}
