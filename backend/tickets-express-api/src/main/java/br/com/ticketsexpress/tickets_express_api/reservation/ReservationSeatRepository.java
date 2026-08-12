package br.com.ticketsexpress.tickets_express_api.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, ReservationSeatId> {

    boolean existsBySeatId(UUID seatId);

    List<ReservationSeat> findByReservationId(UUID reservationId);

    @Query("""
            select case when count(rs) > 0 then true else false end
            from ReservationSeat rs
            where rs.seat.id = :seatId
              and rs.reservation.status in (
                  br.com.ticketsexpress.tickets_express_api.reservation.ReservationStatus.PENDING,
                  br.com.ticketsexpress.tickets_express_api.reservation.ReservationStatus.PAID
              )
            """)
    boolean existsActiveBySeatId(@Param("seatId") UUID seatId);

    @Modifying(flushAutomatically = true)
    @Query("delete from ReservationSeat rs where rs.reservation.id = :reservationId")
    void deleteByReservationId(@Param("reservationId") UUID reservationId);
}
