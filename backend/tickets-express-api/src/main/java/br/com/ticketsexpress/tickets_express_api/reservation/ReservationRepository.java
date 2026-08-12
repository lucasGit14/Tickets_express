package br.com.ticketsexpress.tickets_express_api.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    List<Reservation> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    List<Reservation> findByEventIdOrderByCreatedAtDesc(UUID eventId);

    @Query("""
            select r from Reservation r
            where r.status = br.com.ticketsexpress.tickets_express_api.reservation.ReservationStatus.PENDING
              and r.expiresAt is not null
              and r.expiresAt < :now
            """)
    List<Reservation> findExpiredPending(@Param("now") Instant now);
}
