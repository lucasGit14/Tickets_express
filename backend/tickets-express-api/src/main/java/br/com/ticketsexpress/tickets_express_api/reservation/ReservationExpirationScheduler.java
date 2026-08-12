package br.com.ticketsexpress.tickets_express_api.reservation;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationExpirationScheduler {

    private final ReservationService reservationService;

    public ReservationExpirationScheduler(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Scheduled(fixedDelayString = "${app.reservation-expiration-check-ms:60000}")
    public void expireReservations() {
        reservationService.expirePendingReservations();
    }
}
