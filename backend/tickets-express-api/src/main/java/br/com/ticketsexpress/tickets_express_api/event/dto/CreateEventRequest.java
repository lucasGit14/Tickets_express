package br.com.ticketsexpress.tickets_express_api.event.dto;

import br.com.ticketsexpress.tickets_express_api.event.EventStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateEventRequest(
        @NotNull Long tmdbMovieId,
        @NotBlank String title,
        String posterUrl,
        String synopsis,
        @NotNull Instant startsAt,
        @NotBlank String venue,
        @NotBlank String address,
        @NotNull BigDecimal price,
        EventStatus status
) {
}
