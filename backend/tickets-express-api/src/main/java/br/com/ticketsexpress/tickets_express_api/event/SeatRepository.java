package br.com.ticketsexpress.tickets_express_api.event;

import br.com.ticketsexpress.tickets_express_api.reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, UUID> {
    List<Seat> findByEventIdOrderByRowLabelAscSeatNumberAsc(UUID eventId);
    
    @Query("SELECT s FROM Seat s WHERE s.event.id = :eventId AND s.id NOT IN (SELECT rs.seat.id FROM ReservationSeat rs WHERE rs.reservation.status IN :statuses) LIMIT 1")
    Optional<Seat> findFirstAvailableByEventId(@Param("eventId") UUID eventId, @Param("statuses") List<ReservationStatus> statuses);
}
