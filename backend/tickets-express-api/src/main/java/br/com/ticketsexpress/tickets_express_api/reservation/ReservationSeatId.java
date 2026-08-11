package br.com.ticketsexpress.tickets_express_api.reservation;

import java.io.Serializable;
import java.util.UUID;

public record ReservationSeatId(UUID reservation, UUID seat) implements Serializable {
}
