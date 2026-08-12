package br.com.ticketsexpress.tickets_express_api.event;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findByStatusOrderByStartsAtAsc(EventStatus status);

    List<Event> findByOrganizerIdOrderByStartsAtAsc(UUID organizerId);
}
