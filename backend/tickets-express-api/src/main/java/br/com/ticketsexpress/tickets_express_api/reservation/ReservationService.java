package br.com.ticketsexpress.tickets_express_api.reservation;

import br.com.ticketsexpress.tickets_express_api.auth.ApplicationUser;
import br.com.ticketsexpress.tickets_express_api.auth.CurrentUserService;
import br.com.ticketsexpress.tickets_express_api.auth.UserRole;
import br.com.ticketsexpress.tickets_express_api.event.Event;
import br.com.ticketsexpress.tickets_express_api.event.EventRepository;
import br.com.ticketsexpress.tickets_express_api.event.EventService;
import br.com.ticketsexpress.tickets_express_api.event.EventStatus;
import br.com.ticketsexpress.tickets_express_api.event.Seat;
import br.com.ticketsexpress.tickets_express_api.event.SeatRepository;
import br.com.ticketsexpress.tickets_express_api.shared.ClockProvider;
import br.com.ticketsexpress.tickets_express_api.shared.ForbiddenException;
import br.com.ticketsexpress.tickets_express_api.shared.ResourceNotFoundException;
import br.com.ticketsexpress.tickets_express_api.shared.UuidProvider;
import br.com.ticketsexpress.tickets_express_api.ticket.Ticket;
import br.com.ticketsexpress.tickets_express_api.ticket.TicketCodeService;
import br.com.ticketsexpress.tickets_express_api.ticket.TicketRepository;
import br.com.ticketsexpress.tickets_express_api.ticket.TicketResponse;
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
    private final EventService eventService;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final CurrentUserService currentUserService;
    private final ClockProvider clockProvider;
    private final UuidProvider uuidProvider;
    private final TicketCodeService ticketCodeService;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationSeatRepository reservationSeatRepository,
                              EventRepository eventRepository,
                              EventService eventService,
                              SeatRepository seatRepository,
                              TicketRepository ticketRepository,
                              CurrentUserService currentUserService,
                              ClockProvider clockProvider,
                              UuidProvider uuidProvider,
                              TicketCodeService ticketCodeService) {
        this.reservationRepository = reservationRepository;
        this.reservationSeatRepository = reservationSeatRepository;
        this.eventRepository = eventRepository;
        this.eventService = eventService;
        this.seatRepository = seatRepository;
        this.ticketRepository = ticketRepository;
        this.currentUserService = currentUserService;
        this.clockProvider = clockProvider;
        this.uuidProvider = uuidProvider;
        this.ticketCodeService = ticketCodeService;
    }

    @Transactional
    public ReservationResponse reserveSeats(ReserveSeatsRequest request) {
        expirePendingReservations();

        ApplicationUser customer = requireCustomer();

        if (request.seatIds() == null || request.seatIds().isEmpty()) {
            throw new IllegalArgumentException("Seat list cannot be empty");
        }

        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new IllegalArgumentException("Event is cancelled");
        }
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new IllegalArgumentException("Event is not available for reservations");
        }

        List<Seat> seats = seatRepository.findAllById(request.seatIds());
        if (seats.size() != request.seatIds().size()) {
            throw new ResourceNotFoundException("One or more seats were not found");
        }

        Set<UUID> uniqueSeatIds = seats.stream().map(Seat::getId).collect(Collectors.toSet());
        if (uniqueSeatIds.size() != request.seatIds().size()) {
            throw new IllegalArgumentException("Duplicate seat IDs are not allowed");
        }

        boolean seatsBelongToEvent = seats.stream().allMatch(seat -> seat.getEvent().getId().equals(event.getId()));
        if (!seatsBelongToEvent) {
            throw new IllegalArgumentException("All seats must belong to the selected event");
        }

        for (Seat seat : seats) {
            if (reservationSeatRepository.existsActiveBySeatId(seat.getId())) {
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
            reservationSeatRepository.save(new ReservationSeat(reservation, seat));
        }

        return toResponse(reservation, List.of());
    }

    @Transactional
    public ReservationResponse pay(UUID reservationId) {
        expirePendingReservations();

        ApplicationUser customer = requireCustomer();
        Reservation reservation = getOwnedReservation(reservationId, customer);

        if (reservation.getStatus() == ReservationStatus.PAID) {
            return toResponse(reservation, ticketRepository.findByReservationId(reservation.getId()));
        }
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalArgumentException("Only pending reservations can be paid");
        }

        Instant now = Instant.now(clockProvider.clock());
        if (reservation.getExpiresAt() != null && reservation.getExpiresAt().isBefore(now)) {
            expireReservation(reservation);
            throw new IllegalArgumentException("Reservation has expired");
        }

        List<Seat> seats = reservationSeatRepository.findByReservationId(reservation.getId()).stream()
                .map(ReservationSeat::getSeat)
                .toList();

        Reservation paid = copyReservation(reservation, ReservationStatus.PAID, "SIMULATED-APPROVED");
        reservationRepository.save(paid);

        List<Ticket> tickets = seats.stream()
                .map(seat -> createTicket(paid, seat, customer, now))
                .toList();

        return toResponse(paid, tickets);
    }

    @Transactional
    public ReservationResponse cancel(UUID reservationId) {
        ApplicationUser customer = requireCustomer();
        Reservation reservation = getOwnedReservation(reservationId, customer);

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            return toResponse(reservation, List.of());
        }
        if (reservation.getStatus() == ReservationStatus.PAID) {
            throw new IllegalArgumentException("Paid reservations cannot be cancelled through this endpoint");
        }
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalArgumentException("Only pending reservations can be cancelled");
        }

        Reservation cancelled = copyReservation(reservation, ReservationStatus.CANCELLED, reservation.getPaymentReference());
        reservationRepository.save(cancelled);
        ReservationResponse response = toResponse(cancelled, List.of());
        releaseSeats(cancelled.getId());
        return response;
    }

    @Transactional
    public List<ReservationResponse> listMine() {
        ApplicationUser customer = requireCustomer();
        return reservationRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId()).stream()
                .map(reservation -> toResponse(reservation, ticketRepository.findByReservationId(reservation.getId())))
                .toList();
    }

    @Transactional
    public ReservationResponse getById(UUID reservationId) {
        ApplicationUser user = currentUserService.requireCurrentUser();
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        boolean isOwner = reservation.getCustomer().getId().equals(user.getId());
        boolean isOrganizer = user.getRole() == UserRole.ORGANIZER
                && reservation.getEvent().getOrganizer().getId().equals(user.getId());

        if (!isOwner && !isOrganizer) {
            throw new ForbiddenException("You cannot view this reservation");
        }

        return toResponse(reservation, ticketRepository.findByReservationId(reservation.getId()));
    }

    @Transactional
    public List<ReservationResponse> listReservationsForEvent(UUID eventId) {
        eventService.requireOwnedEvent(eventId);
        return reservationRepository.findByEventIdOrderByCreatedAtDesc(eventId).stream()
                .map(reservation -> toResponse(reservation, ticketRepository.findByReservationId(reservation.getId())))
                .toList();
    }

    @Transactional
    public void expirePendingReservations() {
        Instant now = Instant.now(clockProvider.clock());
        List<Reservation> expiredReservations = reservationRepository.findExpiredPending(now);
        for (Reservation reservation : expiredReservations) {
            expireReservation(reservation);
        }
    }

    private void expireReservation(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            return;
        }
        Reservation expired = copyReservation(reservation, ReservationStatus.EXPIRED, reservation.getPaymentReference());
        reservationRepository.save(expired);
        releaseSeats(expired.getId());
    }

    private void releaseSeats(UUID reservationId) {
        reservationSeatRepository.deleteByReservationId(reservationId);
    }

    private Ticket createTicket(Reservation reservation, Seat seat, ApplicationUser owner, Instant now) {
        String rawCode = uuidProvider.randomUuid().toString().replace("-", "");
        Ticket ticket = new Ticket(
                uuidProvider.randomUuid(),
                reservation,
                seat,
                rawCode,
                ticketCodeService.hashCode(rawCode),
                owner,
                TicketStatus.VALID,
                null,
                null,
                null,
                null,
                now
        );
        return ticketRepository.save(ticket);
    }

    private Reservation getOwnedReservation(UUID reservationId, ApplicationUser customer) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
        if (!reservation.getCustomer().getId().equals(customer.getId())) {
            throw new ForbiddenException("You can only manage your own reservations");
        }
        return reservation;
    }

    private ApplicationUser requireCustomer() {
        ApplicationUser user = currentUserService.requireCurrentUser();
        if (user.getRole() != UserRole.CUSTOMER) {
            throw new ForbiddenException("Only customers can reserve tickets");
        }
        return user;
    }

    private Reservation copyReservation(Reservation source, ReservationStatus status, String paymentReference) {
        return new Reservation(
                source.getId(),
                source.getCustomer(),
                source.getEvent(),
                status,
                source.getExpiresAt(),
                source.getTotalAmount(),
                paymentReference,
                source.getCreatedAt()
        );
    }

    private BigDecimal calculateTotalAmount(Event event, List<Seat> seats) {
        return event.getPrice().multiply(BigDecimal.valueOf(seats.size()));
    }

    private ReservationResponse toResponse(Reservation reservation, List<Ticket> tickets) {
        List<UUID> seatIds = reservationSeatRepository.findByReservationId(reservation.getId()).stream()
                .map(rs -> rs.getSeat().getId())
                .toList();

        // After release, seats may be empty; for paid tickets derive from tickets
        if (seatIds.isEmpty() && !tickets.isEmpty()) {
            seatIds = tickets.stream().map(ticket -> ticket.getSeat().getId()).toList();
        }

        List<TicketResponse> ticketResponses = tickets.stream().map(this::toTicketResponse).toList();

        return new ReservationResponse(
                reservation.getId(),
                reservation.getEvent().getId(),
                reservation.getEvent().getTitle(),
                reservation.getCustomer().getId(),
                reservation.getCustomer().getName(),
                reservation.getCustomer().getEmail(),
                reservation.getStatus(),
                reservation.getTotalAmount(),
                reservation.getExpiresAt(),
                reservation.getCreatedAt(),
                reservation.getPaymentReference(),
                seatIds,
                ticketResponses
        );
    }

    private TicketResponse toTicketResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getCodeRaw(),
                ticket.getReservation().getId(),
                ticket.getReservation().getEvent().getId(),
                ticket.getReservation().getEvent().getTitle(),
                ticket.getSeat().getId(),
                ticket.getSeat().getRowLabel(),
                ticket.getSeat().getSeatNumber(),
                ticket.getSeat().getCategory(),
                ticket.getOwner().getId(),
                ticket.getOwner().getName(),
                ticket.getOwner().getEmail(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                ticket.getValidatedAt()
        );
    }
}
