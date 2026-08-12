package br.com.ticketsexpress.tickets_express_api.event;

import br.com.ticketsexpress.tickets_express_api.auth.ApplicationUser;
import br.com.ticketsexpress.tickets_express_api.auth.CurrentUserService;
import br.com.ticketsexpress.tickets_express_api.auth.UserRole;
import br.com.ticketsexpress.tickets_express_api.event.dto.CreateEventRequest;
import br.com.ticketsexpress.tickets_express_api.event.dto.EventResponse;
import br.com.ticketsexpress.tickets_express_api.event.dto.UpdateEventRequest;
import br.com.ticketsexpress.tickets_express_api.shared.ClockProvider;
import br.com.ticketsexpress.tickets_express_api.shared.ForbiddenException;
import br.com.ticketsexpress.tickets_express_api.shared.ResourceNotFoundException;
import br.com.ticketsexpress.tickets_express_api.shared.UuidProvider;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

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

    public EventService(EventRepository eventRepository,
                        CurrentUserService currentUserService,
                        UuidProvider uuidProvider,
                        ClockProvider clockProvider) {
        this.eventRepository = eventRepository;
        this.currentUserService = currentUserService;
        this.uuidProvider = uuidProvider;
        this.clockProvider = clockProvider;
    }

    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        ApplicationUser user = requireOrganizer();

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

        return toResponse(eventRepository.save(event));
    }

    @Transactional
    public List<EventResponse> listPublishedEvents() {
        return eventRepository.findByStatusOrderByStartsAtAsc(EventStatus.PUBLISHED).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<EventResponse> listMyEvents() {
        ApplicationUser user = requireOrganizer();
        return eventRepository.findByOrganizerIdOrderByStartsAtAsc(user.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventResponse getEvent(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (!canViewEvent(event)) {
            throw new ResourceNotFoundException("Event not found");
        }

        return toResponse(event);
    }

    @Transactional
    public EventResponse updateEvent(UUID id, UpdateEventRequest request) {
        Event existing = requireOwnedEvent(id);

        if (existing.getStatus() == EventStatus.CANCELLED) {
            throw new IllegalArgumentException("Cancelled events cannot be edited");
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

        return toResponse(eventRepository.save(updated));
    }

    @Transactional
    public EventResponse publishEvent(UUID id) {
        Event existing = requireOwnedEvent(id);
        if (existing.getStatus() == EventStatus.CANCELLED) {
            throw new IllegalArgumentException("Cancelled events cannot be published");
        }
        if (existing.getStatus() == EventStatus.PUBLISHED) {
            return toResponse(existing);
        }

        Event published = copyWithStatus(existing, EventStatus.PUBLISHED);
        return toResponse(eventRepository.save(published));
    }

    @Transactional
    public EventResponse cancelEvent(UUID id) {
        Event existing = requireOwnedEvent(id);
        if (existing.getStatus() == EventStatus.CANCELLED) {
            return toResponse(existing);
        }

        Event cancelled = copyWithStatus(existing, EventStatus.CANCELLED);
        return toResponse(eventRepository.save(cancelled));
    }

    public Event requireOwnedEvent(UUID id) {
        ApplicationUser user = requireOrganizer();
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (!event.getOrganizer().getId().equals(user.getId())) {
            throw new ForbiddenException("You can only manage your own events");
        }

        return event;
    }

    public Event requireEventEntity(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    private boolean canViewEvent(Event event) {
        if (event.getStatus() == EventStatus.PUBLISHED) {
            return true;
        }

        ApplicationUser user = currentUserService.findCurrentUserOrNull();
        if (user == null) {
            return false;
        }

        if (user.getRole() == UserRole.GATEKEEPER) {
            return true;
        }

        return user.getRole() == UserRole.ORGANIZER
                && event.getOrganizer().getId().equals(user.getId());
    }

    private ApplicationUser requireOrganizer() {
        ApplicationUser user = currentUserService.requireCurrentUser();
        if (user.getRole() != UserRole.ORGANIZER) {
            throw new ForbiddenException("Only organizers can perform this action");
        }
        return user;
    }

    private Event copyWithStatus(Event existing, EventStatus status) {
        return new Event(
                existing.getId(),
                existing.getOrganizer(),
                existing.getTmdbMovieId(),
                existing.getTitle(),
                existing.getPosterUrl(),
                existing.getSynopsis(),
                existing.getStartsAt(),
                existing.getVenue(),
                existing.getAddress(),
                existing.getPrice(),
                status,
                existing.getCreatedAt()
        );
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
