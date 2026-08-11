package br.com.ticketsexpress.tickets_express_api.event;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, UUID> {
    List<Seat> findByEventIdOrderByRowLabelAscSeatNumberAsc(UUID eventId);
}
