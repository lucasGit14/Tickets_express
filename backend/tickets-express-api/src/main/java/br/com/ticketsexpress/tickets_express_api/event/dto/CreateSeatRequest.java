package br.com.ticketsexpress.tickets_express_api.event.dto;

import br.com.ticketsexpress.tickets_express_api.event.SeatCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSeatRequest(
        @NotBlank String rowLabel,
        @NotNull Integer seatNumber,
        @NotNull SeatCategory category
) {
}
