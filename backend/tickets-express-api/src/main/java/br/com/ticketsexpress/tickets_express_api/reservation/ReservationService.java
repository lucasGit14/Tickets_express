package br.com.ticketsexpress.tickets_express_api.reservation;

import br.com.ticketsexpress.tickets_express_api.auth.ApplicationUser;
import br.com.ticketsexpress.tickets_express_api.event.Event;
import br.com.ticketsexpress.tickets_express_api.event.Seat;
import br.com.ticketsexpress.tickets_express_api.event.SeatRepository;
import br.com.ticketsexpress.tickets_express_api.shared.ClockProvider;
import br.com.ticketsexpress.tickets_express_api.shared.UuidProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {

    private static final Duration RESERVATION_TTL = Duration.ofMinutes(15);

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final ClockProvider clockProvider;
    private final UuidProvider uuidProvider;

    public ReservationService(ReservationRepository reservationRepository,
                              SeatRepository seatRepository,
                              ClockProvider clockProvider,
                              UuidProvider uuidProvider) {
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
        this.clockProvider = clockProvider;
        this.uuidProvider = uuidProvider;
    }

    public Reservation createPendingReservation(ApplicationUser customer,
                                                 Event event,
                                                 List<Seat> seats,
                                                 BigDecimal totalAmount) {
        if (seats == null || seats.isEmpty()) {
            throw new IllegalArgumentException("At least one seat is required");
        }

        Instant now = clockProvider.now();
        Instant expiresAt = now.plus(RESERVATION_TTL);

        Reservation reservation = new Reservation(
                uuidProvider.nextId(),
                customer,
                event,
                ReservationStatus.PENDING,
                expiresAt,
                totalAmount,
                null,
                now
        );

        reservationRepository.save(reservation);
        return reservation;
    }
}
