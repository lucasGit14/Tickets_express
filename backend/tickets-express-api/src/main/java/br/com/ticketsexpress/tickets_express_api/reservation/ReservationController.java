package br.com.ticketsexpress.tickets_express_api.reservation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> reserve(@Valid @RequestBody ReserveSeatsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.reserveSeats(request));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<ReservationResponse> pay(@PathVariable UUID id) {
        return ResponseEntity.ok(reservationService.pay(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponse> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(reservationService.cancel(id));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ReservationResponse>> me() {
        return ResponseEntity.ok(reservationService.listMine());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(reservationService.getById(id));
    }
}
