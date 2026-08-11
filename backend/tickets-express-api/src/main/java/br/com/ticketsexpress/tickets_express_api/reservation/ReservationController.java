package br.com.ticketsexpress.tickets_express_api.reservation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReserveSeatsResponse> reserve(@Valid @RequestBody ReserveSeatsRequest request) {
        ReserveSeatsResponse response = reservationService.reserveSeats(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
