package br.com.ticketsexpress.tickets_express_api.reservation;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ReserveSeatsRequest(
        @NotNull UUID eventId,
        @NotEmpty List<UUID> seatIds
) {
}
