package br.com.ticketsexpress.tickets_express_api.event;

import br.com.ticketsexpress.tickets_express_api.auth.ApplicationUser;
import br.com.ticketsexpress.tickets_express_api.auth.CurrentUserService;
import br.com.ticketsexpress.tickets_express_api.auth.UserRole;
import br.com.ticketsexpress.tickets_express_api.event.dto.CreateEventRequest;
import br.com.ticketsexpress.tickets_express_api.event.dto.EventResponse;
import br.com.ticketsexpress.tickets_express_api.event.dto.UpdateEventRequest;
import br.com.ticketsexpress.tickets_express_api.shared.ClockProvider;
import br.com.ticketsexpress.tickets_express_api.shared.UuidProvider;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final CurrentUserService currentUserService;
    private final UuidProvider uuidProvider;
    private final ClockProvider clockProvider;

    public EventService(EventRepository eventRepository, CurrentUserService currentUserService, UuidProvider uuidProvider, ClockProvider clockProvider) {
        this.eventRepository = eventRepository;
        this.currentUserService = currentUserService;
        this.uuidProvider = uuidProvider;
        this.clockProvider = clockProvider;
    }

    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        ApplicationUser user = currentUserService.requireCurrentUser();
        if (user.getRole() != UserRole.ORGANIZER) {
            throw new IllegalArgumentException("Only organizers can create events");
        }

        EventStatus status = request.status() == null ? EventStatus.DRAFT : request.status();
        Instant now = Instant.now(clockProvider.clock());
        Event event = new Event(
                uuidProvider.randomUuid(),
                user,
                request.tmdbMovieId(),
                request.title(),
                request.posterUrl(),
                request.synopsis(),
                request.startsAt(),
                request.venue(),
                request.address(),
                request.price(),
                status,
                now
        );

        Event saved = eventRepository.save(event);
        return toResponse(saved);
    }

    public List<EventResponse> listPublishedEvents() {
        return eventRepository.findByStatusOrderByStartsAtAsc(EventStatus.PUBLISHED).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public EventResponse getEvent(UUID id) {
        return eventRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
    }

    @Transactional
    public EventResponse updateEvent(UUID id, UpdateEventRequest request) {
        ApplicationUser user = currentUserService.requireCurrentUser();
        Event existing = eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Event not found"));
        if (!existing.getOrganizer().getId().equals(user.getId()) && user.getRole() != UserRole.ORGANIZER) {
            throw new IllegalArgumentException("Only the organizer who created the event can update it");
        }

        Event updated = new Event(
                existing.getId(),
                existing.getOrganizer(),
                request.tmdbMovieId() == null ? existing.getTmdbMovieId() : request.tmdbMovieId(),
                request.title() == null ? existing.getTitle() : request.title(),
                request.posterUrl() == null ? existing.getPosterUrl() : request.posterUrl(),
                request.synopsis() == null ? existing.getSynopsis() : request.synopsis(),
                request.startsAt() == null ? existing.getStartsAt() : request.startsAt(),
                request.venue() == null ? existing.getVenue() : request.venue(),
                request.address() == null ? existing.getAddress() : request.address(),
                request.price() == null ? existing.getPrice() : request.price(),
                request.status() == null ? existing.getStatus() : request.status(),
                existing.getCreatedAt()
        );

        Event saved = eventRepository.save(updated);
        return toResponse(saved);
    }

    private EventResponse toResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getOrganizer().getId(),
                event.getTmdbMovieId(),
                event.getTitle(),
                event.getPosterUrl(),
                event.getSynopsis(),
                event.getStartsAt(),
                event.getVenue(),
                event.getAddress(),
                event.getPrice(),
                event.getStatus(),
                event.getCreatedAt()
        );
    }
}
