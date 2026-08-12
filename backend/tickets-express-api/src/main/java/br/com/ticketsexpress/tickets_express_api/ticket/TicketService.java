package br.com.ticketsexpress.tickets_express_api.ticket;

import br.com.ticketsexpress.tickets_express_api.auth.ApplicationUser;
import br.com.ticketsexpress.tickets_express_api.auth.CurrentUserService;
import br.com.ticketsexpress.tickets_express_api.auth.UserRepository;
import br.com.ticketsexpress.tickets_express_api.auth.UserRole;
import br.com.ticketsexpress.tickets_express_api.event.Event;
import br.com.ticketsexpress.tickets_express_api.event.EventRepository;
import br.com.ticketsexpress.tickets_express_api.event.EventStatus;
import br.com.ticketsexpress.tickets_express_api.event.Seat;
import br.com.ticketsexpress.tickets_express_api.event.SeatRepository;
import br.com.ticketsexpress.tickets_express_api.reservation.Reservation;
import br.com.ticketsexpress.tickets_express_api.reservation.ReservationRepository;
import br.com.ticketsexpress.tickets_express_api.reservation.ReservationSeatRepository;
import br.com.ticketsexpress.tickets_express_api.reservation.ReservationService;
import br.com.ticketsexpress.tickets_express_api.reservation.ReservationStatus;
import br.com.ticketsexpress.tickets_express_api.reservation.ReserveSeatsRequest;
import br.com.ticketsexpress.tickets_express_api.reservation.ReservationResponse;
import br.com.ticketsexpress.tickets_express_api.shared.ClockProvider;
import br.com.ticketsexpress.tickets_express_api.shared.ForbiddenException;
import br.com.ticketsexpress.tickets_express_api.shared.ResourceNotFoundException;
import br.com.ticketsexpress.tickets_express_api.shared.UuidProvider;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketTransferRepository ticketTransferRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final ClockProvider clockProvider;
    private final UuidProvider uuidProvider;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final ReservationService reservationService;

    public TicketService(TicketRepository ticketRepository,
                         TicketTransferRepository ticketTransferRepository,
                         UserRepository userRepository,
                         CurrentUserService currentUserService,
                         ClockProvider clockProvider,
                         UuidProvider uuidProvider,
                         EventRepository eventRepository,
                         SeatRepository seatRepository,
                         ReservationRepository reservationRepository,
                         ReservationSeatRepository reservationSeatRepository,
                         ReservationService reservationService) {
        this.ticketRepository = ticketRepository;
        this.ticketTransferRepository = ticketTransferRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.clockProvider = clockProvider;
        this.uuidProvider = uuidProvider;
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.reservationSeatRepository = reservationSeatRepository;
        this.reservationService = reservationService;
    }

    @Transactional
    public List<TicketResponse> listMine() {
        ApplicationUser user = currentUserService.requireCurrentUser();
        return ticketRepository.findByOwnerIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TicketResponse getById(UUID id) {
        ApplicationUser user = currentUserService.requireCurrentUser();
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        boolean isOwner = ticket.getOwner().getId().equals(user.getId());
        boolean isOrganizer = user.getRole() == UserRole.ORGANIZER
                && ticket.getReservation().getEvent().getOrganizer().getId().equals(user.getId());
        boolean isGatekeeper = user.getRole() == UserRole.GATEKEEPER;

        if (!isOwner && !isOrganizer && !isGatekeeper) {
            throw new ForbiddenException("You cannot view this ticket");
        }

        return toResponse(ticket);
    }

    @Transactional
    public TicketResponse validate(ValidateTicketRequest request) {
        ApplicationUser gatekeeper = currentUserService.requireCurrentUser();
        if (gatekeeper.getRole() != UserRole.GATEKEEPER) {
            throw new ForbiddenException("Only gatekeepers can validate tickets");
        }

        Ticket ticket = ticketRepository.findByCodeRaw(request.code().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        ReservationStatus reservationStatus = ticket.getReservation().getStatus();
        if (reservationStatus == ReservationStatus.CANCELLED || reservationStatus == ReservationStatus.EXPIRED) {
            throw new IllegalArgumentException("Ticket reservation is not valid");
        }
        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new IllegalArgumentException("Ticket is cancelled");
        }
        if (ticket.getStatus() == TicketStatus.USED) {
            throw new IllegalArgumentException("Ticket already used");
        }
        if (ticket.getStatus() != TicketStatus.VALID) {
            throw new IllegalArgumentException("Ticket is not valid");
        }

        Instant now = Instant.now(clockProvider.clock());
        Ticket used = new Ticket(
                ticket.getId(),
                ticket.getReservation(),
                ticket.getSeat(),
                ticket.getCodeRaw(),
                ticket.getCodeHash(),
                ticket.getOwner(),
                TicketStatus.USED,
                now,
                gatekeeper,
                ticket.getShareTokenHash(),
                ticket.getShareExpiresAt(),
                ticket.getCreatedAt()
        );

        return toResponse(ticketRepository.save(used));
    }

    @Transactional
    public TicketResponse transfer(UUID ticketId, TransferTicketRequest request) {
        ApplicationUser currentOwner = currentUserService.requireCurrentUser();
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        if (!ticket.getOwner().getId().equals(currentOwner.getId())) {
            throw new ForbiddenException("Only the current owner can transfer this ticket");
        }
        if (ticket.getStatus() == TicketStatus.USED) {
            throw new IllegalArgumentException("Used tickets cannot be transferred");
        }
        if (ticket.getStatus() != TicketStatus.VALID) {
            throw new IllegalArgumentException("Only valid tickets can be transferred");
        }

        ApplicationUser newOwner = userRepository.findByEmail(request.email().trim().toLowerCase())
                .or(() -> userRepository.findByEmail(request.email().trim()))
                .orElseThrow(() -> new ResourceNotFoundException("Destination user not found"));

        if (newOwner.getRole() != UserRole.CUSTOMER) {
            throw new IllegalArgumentException("New owner must be a CUSTOMER");
        }
        if (newOwner.getId().equals(currentOwner.getId())) {
            throw new IllegalArgumentException("Cannot transfer ticket to yourself");
        }

        Instant now = Instant.now(clockProvider.clock());
        Ticket transferred = new Ticket(
                ticket.getId(),
                ticket.getReservation(),
                ticket.getSeat(),
                ticket.getCodeRaw(),
                ticket.getCodeHash(),
                newOwner,
                ticket.getStatus(),
                ticket.getValidatedAt(),
                ticket.getValidatedBy(),
                ticket.getShareTokenHash(),
                ticket.getShareExpiresAt(),
                ticket.getCreatedAt()
        );

        Ticket saved = ticketRepository.save(transferred);
        ticketTransferRepository.save(new TicketTransfer(
                uuidProvider.randomUuid(),
                saved,
                currentOwner,
                newOwner,
                now
        ));

        return toResponse(saved);
    }

    @Transactional
    public TicketResponse purchase(UUID eventId, PurchaseRequest request) {
        ApplicationUser customer = currentUserService.requireCurrentUser();
        if (customer.getRole() != UserRole.CUSTOMER) {
            throw new ForbiddenException("Only customers can purchase tickets");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new IllegalArgumentException("Event is cancelled");
        }
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new IllegalArgumentException("Event is not available for purchase");
        }

        Seat availableSeat = seatRepository.findFirstAvailableByEventId(eventId, List.of(ReservationStatus.PENDING, ReservationStatus.PAID))
                .orElseThrow(() -> new IllegalArgumentException("No tickets available for this event"));

        ReserveSeatsRequest reserveRequest = new ReserveSeatsRequest(eventId, List.of(availableSeat.getId()));
        ReservationResponse reservationResponse = reservationService.reserveSeats(reserveRequest);

        ReservationResponse paidResponse = reservationService.pay(reservationResponse.id());
        
        return paidResponse.tickets().get(0);
    }

    private TicketResponse toResponse(Ticket ticket) {
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
