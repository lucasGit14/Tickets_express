package br.com.ticketsexpress.tickets_express_api.integration;

import br.com.ticketsexpress.tickets_express_api.auth.ApplicationUser;
import br.com.ticketsexpress.tickets_express_api.auth.UserRepository;
import br.com.ticketsexpress.tickets_express_api.auth.UserRole;
import br.com.ticketsexpress.tickets_express_api.event.EventRepository;
import br.com.ticketsexpress.tickets_express_api.event.SeatRepository;
import br.com.ticketsexpress.tickets_express_api.event.dto.CreateEventRequest;
import br.com.ticketsexpress.tickets_express_api.event.dto.CreateSeatRequest;
import br.com.ticketsexpress.tickets_express_api.reservation.ReserveSeatsRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
public class ReservationFlowIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Test
    void fullReservationFlow() {
        String organizerEmail = "org-" + UUID.randomUUID() + "@example.com";
        String customerEmail = "cust-" + UUID.randomUUID() + "@example.com";

        // create organizer
        ApplicationUser organizer = new ApplicationUser(UUID.randomUUID(), "Org", organizerEmail, passwordEncoder.encode("password"), UserRole.ORGANIZER, Instant.now());
        userRepository.save(organizer);

        // login organizer
        ResponseEntity<Map> loginResp = rest.postForEntity("/api/auth/login", Map.of("email", organizerEmail, "password", "password"), Map.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        String orgToken = (String) loginResp.getBody().get("token");
        assertThat(orgToken).isNotBlank();

        // create event
        CreateEventRequest createEvent = new CreateEventRequest(123L, "My Event", null, null, Instant.now().plusSeconds(3600), "Venue", "Address", new BigDecimal("10.00"), null);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(orgToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateEventRequest> eventReq = new HttpEntity<>(createEvent, headers);

        ResponseEntity<Map> eventResp = rest.exchange("/api/events", HttpMethod.POST, eventReq, Map.class);
        assertThat(eventResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Map body = eventResp.getBody();
        UUID eventId = UUID.fromString((String) body.get("id"));

        // create seats
        CreateSeatRequest seat1 = new CreateSeatRequest("A", 1, null);
        CreateSeatRequest seat2 = new CreateSeatRequest("A", 2, null);
        HttpEntity<List<CreateSeatRequest>> seatsReq = new HttpEntity<>(List.of(seat1, seat2), headers);
        ResponseEntity<List> seatsResp = rest.exchange("/api/events/" + eventId + "/seats", HttpMethod.POST, seatsReq, List.class);
        assertThat(seatsResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        List seats = seatsResp.getBody();
        assertThat(seats).hasSize(2);
        String seatId1 = (String) ((Map) seats.get(0)).get("id");
        String seatId2 = (String) ((Map) seats.get(1)).get("id");

        // create customer
        ApplicationUser customer = new ApplicationUser(UUID.randomUUID(), "Cust", customerEmail, passwordEncoder.encode("password"), UserRole.CUSTOMER, Instant.now());
        userRepository.save(customer);

        // login customer
        ResponseEntity<Map> loginCust = rest.postForEntity("/api/auth/login", Map.of("email", customerEmail, "password", "password"), Map.class);
        assertThat(loginCust.getStatusCode()).isEqualTo(HttpStatus.OK);
        String custToken = (String) loginCust.getBody().get("token");

        // reserve seats
        HttpHeaders custHeaders = new HttpHeaders();
        custHeaders.setBearerAuth(custToken);
        custHeaders.setContentType(MediaType.APPLICATION_JSON);

        ReserveSeatsRequest reserveReq = new ReserveSeatsRequest(eventId, List.of(UUID.fromString(seatId1), UUID.fromString(seatId2)));
        HttpEntity<ReserveSeatsRequest> reserveEntity = new HttpEntity<>(reserveReq, custHeaders);

        ResponseEntity<Map> reserveResp = rest.exchange("/api/reservations", HttpMethod.POST, reserveEntity, Map.class);
        assertThat(reserveResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Map reserveBody = reserveResp.getBody();
        assertThat(reserveBody.get("status")).isEqualTo("PAID");
    }
}
