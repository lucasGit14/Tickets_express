package br.com.ticketsexpress.tickets_express_api.reservation;

import br.com.ticketsexpress.tickets_express_api.event.Seat;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "reservation_seats")
@IdClass(ReservationSeat.ReservationSeatId.class)
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

    public static class ReservationSeatId implements Serializable {
        private UUID reservation;
        private UUID seat;

        public ReservationSeatId() {
        }

        public ReservationSeatId(UUID reservation, UUID seat) {
            this.reservation = reservation;
            this.seat = seat;
        }

        public UUID getReservation() {
            return reservation;
        }

        public UUID getSeat() {
            return seat;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ReservationSeatId that)) {
                return false;
            }
            return Objects.equals(reservation, that.reservation)
                    && Objects.equals(seat, that.seat);
        }

        @Override
        public int hashCode() {
            return Objects.hash(reservation, seat);
        }
    }
}
