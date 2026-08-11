package br.com.ticketsexpress.tickets_express_api.reservation;

import br.com.ticketsexpress.tickets_express_api.auth.ApplicationUser;
import br.com.ticketsexpress.tickets_express_api.auth.CurrentUserService;
import br.com.ticketsexpress.tickets_express_api.event.Event;
import br.com.ticketsexpress.tickets_express_api.event.EventRepository;
import br.com.ticketsexpress.tickets_express_api.event.EventStatus;
import br.com.ticketsexpress.tickets_express_api.event.Seat;
import br.com.ticketsexpress.tickets_express_api.event.SeatRepository;
import br.com.ticketsexpress.tickets_express_api.shared.ClockProvider;
import br.com.ticketsexpress.tickets_express_api.shared.UuidProvider;
import br.com.ticketsexpress.tickets_express_api.ticket.Ticket;
import br.com.ticketsexpress.tickets_express_api.ticket.TicketCodeService;
import br.com.ticketsexpress.tickets_express_api.ticket.TicketRepository;
import br.com.ticketsexpress.tickets_express_api.ticket.TicketStatus;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private static final Duration RESERVATION_EXPIRATION = Duration.ofMinutes(15);

    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final CurrentUserService currentUserService;
    private final ClockProvider clockProvider;
    private final UuidProvider uuidProvider;
    private final TicketCodeService ticketCodeService;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationSeatRepository reservationSeatRepository,
                              EventRepository eventRepository,
                              SeatRepository seatRepository,
                              TicketRepository ticketRepository,
                              CurrentUserService currentUserService,
                              ClockProvider clockProvider,
                              UuidProvider uuidProvider,
                              TicketCodeService ticketCodeService) {
        this.reservationRepository = reservationRepository;
        this.reservationSeatRepository = reservationSeatRepository;
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
        this.ticketRepository = ticketRepository;
        this.currentUserService = currentUserService;
        this.clockProvider = clockProvider;
        this.uuidProvider = uuidProvider;
        this.ticketCodeService = ticketCodeService;
    }

    @Transactional
    public ReserveSeatsResponse reserveSeats(ReserveSeatsRequest request) {
        ApplicationUser customer = requireCurrentUser();
        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new IllegalArgumentException("Event is not available for reservations");
        }

        List<Seat> seats = seatRepository.findAllById(request.seatIds());
        if (seats.size() != request.seatIds().size()) {
            throw new IllegalArgumentException("One or more seats were not found");
        }

        Set<UUID> seatIds = seats.stream()
                .map(Seat::getId)
                .collect(Collectors.toSet());

        if (seatIds.size() != request.seatIds().size()) {
            throw new IllegalArgumentException("Duplicate seat IDs are not allowed");
        }

        boolean seatsBelongToEvent = seats.stream().allMatch(seat -> seat.getEvent().getId().equals(event.getId()));
        if (!seatsBelongToEvent) {
            throw new IllegalArgumentException("All seats must belong to the selected event");
        }

        for (Seat seat : seats) {
            if (reservationSeatRepository.existsBySeatId(seat.getId())) {
                throw new IllegalArgumentException("One or more seats are already reserved");
            }
        }

        Instant now = Instant.now(clockProvider.clock());
        BigDecimal totalAmount = calculateTotalAmount(event, seats);

        Reservation reservation = new Reservation(
                uuidProvider.randomUuid(),
                customer,
                event,
                ReservationStatus.PENDING,
                now.plus(RESERVATION_EXPIRATION),
                totalAmount,
                null,
                now
        );

        reservation = reservationRepository.save(reservation);

        for (Seat seat : seats) {
            ReservationSeat reservationSeat = new ReservationSeat(reservation, seat);
            reservationSeatRepository.save(reservationSeat);
        }

        if (shouldApprovePayment(request)) {
            approveReservation(reservation, seats, now);
        }

        return new ReserveSeatsResponse(
                reservation.getId(),
                reservation.getTotalAmount(),
                reservation.getStatus().name()
        );
    }

    @Transactional
    public void expirePendingReservations() {
        Instant now = Instant.now(clockProvider.clock());
        List<Reservation> expiredReservations = reservationRepository.findAll().stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.PENDING)
                .filter(reservation -> reservation.getExpiresAt() != null && reservation.getExpiresAt().isBefore(now))
                .toList();

        for (Reservation reservation : expiredReservations) {
            Reservation expired = new Reservation(
                    reservation.getId(),
                    reservation.getCustomer(),
                    reservation.getEvent(),
                    ReservationStatus.EXPIRED,
                    reservation.getExpiresAt(),
                    reservation.getTotalAmount(),
                    reservation.getPaymentReference(),
                    reservation.getCreatedAt()
            );
            reservationRepository.save(expired);
        }
    }

    private void approveReservation(Reservation reservation, List<Seat> seats, Instant now) {
        Reservation approved = new Reservation(
                reservation.getId(),
                reservation.getCustomer(),
                reservation.getEvent(),
                ReservationStatus.PAID,
                reservation.getExpiresAt(),
                reservation.getTotalAmount(),
                "SIMULATED-APPROVED",
                reservation.getCreatedAt()
        );

        reservationRepository.save(approved);

        for (Seat seat : seats) {
            Ticket ticket = new Ticket(
                    uuidProvider.randomUuid(),
                    approved,
                    seat,
                    ticketCodeService.hashCode(uuidProvider.randomUuid().toString()),
                    TicketStatus.ACTIVE,
                    null,
                    null,
                    null,
                    null,
                    now
            );
            ticketRepository.save(ticket);
        }
    }

    private boolean shouldApprovePayment(ReserveSeatsRequest request) {
        return request.seatIds() != null && !request.seatIds().isEmpty();
    }

    private BigDecimal calculateTotalAmount(Event event, List<Seat> seats) {
        return event.getPrice().multiply(BigDecimal.valueOf(seats.size()));
    }

    private ApplicationUser requireCurrentUser() {
        throw new UnsupportedOperationException("Current user lookup not implemented yet");
    }
}
