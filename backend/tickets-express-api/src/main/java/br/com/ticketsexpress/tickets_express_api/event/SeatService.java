package br.com.ticketsexpress.tickets_express_api.event;

import br.com.ticketsexpress.tickets_express_api.auth.ApplicationUser;
import br.com.ticketsexpress.tickets_express_api.auth.CurrentUserService;
import br.com.ticketsexpress.tickets_express_api.auth.UserRole;
import br.com.ticketsexpress.tickets_express_api.event.dto.CreateSeatRequest;
import br.com.ticketsexpress.tickets_express_api.event.dto.SeatResponse;
import br.com.ticketsexpress.tickets_express_api.reservation.ReservationSeatRepository;
import br.com.ticketsexpress.tickets_express_api.shared.ForbiddenException;
import br.com.ticketsexpress.tickets_express_api.shared.ResourceNotFoundException;
import br.com.ticketsexpress.tickets_express_api.shared.UuidProvider;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SeatService {

    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;
    private final ReservationSeatRepository reservationSeatRepository;
    private final CurrentUserService currentUserService;
    private final UuidProvider uuidProvider;

    public SeatService(SeatRepository seatRepository,
                       EventRepository eventRepository,
                       EventService eventService,
                       ReservationSeatRepository reservationSeatRepository,
                       CurrentUserService currentUserService,
                       UuidProvider uuidProvider) {
        this.seatRepository = seatRepository;
        this.eventRepository = eventRepository;
        this.eventService = eventService;
        this.reservationSeatRepository = reservationSeatRepository;
        this.currentUserService = currentUserService;
        this.uuidProvider = uuidProvider;
    }

    @Transactional
    public List<SeatResponse> createSeats(UUID eventId, List<CreateSeatRequest> requests) {
        ApplicationUser user = currentUserService.requireCurrentUser();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (user.getRole() != UserRole.ORGANIZER || !event.getOrganizer().getId().equals(user.getId())) {
            throw new ForbiddenException("Only the organizer who created the event can add seats");
        }

        List<Seat> existing = seatRepository.findByEventIdOrderByRowLabelAscSeatNumberAsc(eventId);

        for (CreateSeatRequest req : requests) {
            boolean conflict = existing.stream()
                    .anyMatch(s -> s.getRowLabel().equals(req.rowLabel()) && s.getSeatNumber().equals(req.seatNumber()));
            if (conflict) {
                throw new IllegalArgumentException("One or more seats already exist");
            }
        }

        List<Seat> toSave = requests.stream().map(req -> new Seat(
                uuidProvider.randomUuid(),
                event,
                req.rowLabel(),
                req.seatNumber(),
                req.category() == null ? SeatCategory.STANDARD : req.category()
        )).collect(Collectors.toList());

        return seatRepository.saveAll(toSave).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public List<SeatResponse> listSeatsByEvent(UUID eventId) {
        eventService.getEvent(eventId);
        return seatRepository.findByEventIdOrderByRowLabelAscSeatNumberAsc(eventId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private SeatResponse toResponse(Seat seat) {
        boolean available = !reservationSeatRepository.existsActiveBySeatId(seat.getId());
        return new SeatResponse(
                seat.getId(),
                seat.getEvent().getId(),
                seat.getRowLabel(),
                seat.getSeatNumber(),
                seat.getCategory(),
                available
        );
    }
}
