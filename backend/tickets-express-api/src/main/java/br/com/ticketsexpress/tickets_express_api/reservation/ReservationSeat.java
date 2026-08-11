package br.com.ticketsexpress.tickets_express_api.reservation;

import br.com.ticketsexpress.tickets_express_api.event.Seat;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservation_seats")
@IdClass(ReservationSeatId.class)
public class ReservationSeat {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    public ReservationSeat() {
    }

    public ReservationSeat(Reservation reservation, Seat seat) {
        this.reservation = reservation;
        this.seat = seat;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Seat getSeat() {
        return seat;
    }
}
