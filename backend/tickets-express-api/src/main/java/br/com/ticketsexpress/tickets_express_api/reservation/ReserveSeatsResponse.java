package br.com.ticketsexpress.tickets_express_api.reservation;

import java.math.BigDecimal;
import java.util.UUID;

public record ReserveSeatsResponse(
        UUID reservationId,
        BigDecimal totalAmount,
        String status
) {
}
