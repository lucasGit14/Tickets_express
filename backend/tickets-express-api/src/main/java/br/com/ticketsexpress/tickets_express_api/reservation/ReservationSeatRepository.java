package br.com.ticketsexpress.tickets_express_api.reservation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, ReservationSeatId> {
    boolean existsBySeatId(UUID seatId);
}
