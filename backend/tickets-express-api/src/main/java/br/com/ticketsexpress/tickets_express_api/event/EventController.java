package br.com.ticketsexpress.tickets_express_api.event;

import br.com.ticketsexpress.tickets_express_api.event.dto.CreateEventRequest;
import br.com.ticketsexpress.tickets_express_api.event.dto.EventResponse;
import br.com.ticketsexpress.tickets_express_api.event.dto.UpdateEventRequest;
import br.com.ticketsexpress.tickets_express_api.reservation.ReservationResponse;
import br.com.ticketsexpress.tickets_express_api.reservation.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final ReservationService reservationService;

    public EventController(EventService eventService, ReservationService reservationService) {
        this.eventService = eventService;
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest request) {
        EventResponse response = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> list() {
        return ResponseEntity.ok(eventService.listPublishedEvents());
    }

    @GetMapping("/mine")
    public ResponseEntity<List<EventResponse>> mine() {
        return ResponseEntity.ok(eventService.listMyEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.getEvent(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> update(@PathVariable UUID id, @RequestBody UpdateEventRequest request) {
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<EventResponse> publish(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.publishEvent(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<EventResponse> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.cancelEvent(id));
    }

    @GetMapping("/{id}/reservations")
    public ResponseEntity<List<ReservationResponse>> reservations(@PathVariable UUID id) {
        return ResponseEntity.ok(reservationService.listReservationsForEvent(id));
    }
}
