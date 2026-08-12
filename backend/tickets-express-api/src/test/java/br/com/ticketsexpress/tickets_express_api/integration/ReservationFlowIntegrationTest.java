package br.com.ticketsexpress.tickets_express_api.integration;

import br.com.ticketsexpress.tickets_express_api.auth.ApplicationUser;
import br.com.ticketsexpress.tickets_express_api.auth.UserRepository;
import br.com.ticketsexpress.tickets_express_api.auth.UserRole;
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

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void fullReservationFlow() {
        String organizerEmail = "org-" + UUID.randomUUID() + "@example.com";
        String customerEmail = "cust-" + UUID.randomUUID() + "@example.com";

        ApplicationUser organizer = new ApplicationUser(
                UUID.randomUUID(), "Org", organizerEmail, passwordEncoder.encode("password"), UserRole.ORGANIZER, Instant.now());
        userRepository.save(organizer);

        ResponseEntity<Map> loginResp = rest.postForEntity(
                "/api/auth/login", Map.of("email", organizerEmail, "password", "password"), Map.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        String orgToken = (String) loginResp.getBody().get("token");

        CreateEventRequest createEvent = new CreateEventRequest(
                123L, "My Event", null, null, Instant.now().plusSeconds(3600),
                "Venue", "Address", new BigDecimal("10.00"), null);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(orgToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> eventResp = rest.exchange(
                "/api/events", HttpMethod.POST, new HttpEntity<>(createEvent, headers), Map.class);
        assertThat(eventResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID eventId = UUID.fromString((String) eventResp.getBody().get("id"));
        assertThat(eventResp.getBody().get("status")).isEqualTo("DRAFT");

        ResponseEntity<Map> publishResp = rest.exchange(
                "/api/events/" + eventId + "/publish", HttpMethod.POST, new HttpEntity<>(null, headers), Map.class);
        assertThat(publishResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(publishResp.getBody().get("status")).isEqualTo("PUBLISHED");

        CreateSeatRequest seat1 = new CreateSeatRequest("A", 1, null);
        CreateSeatRequest seat2 = new CreateSeatRequest("A", 2, null);
        ResponseEntity<List> seatsResp = rest.exchange(
                "/api/events/" + eventId + "/seats",
                HttpMethod.POST,
                new HttpEntity<>(List.of(seat1, seat2), headers),
                List.class);
        assertThat(seatsResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        List seats = seatsResp.getBody();
        String seatId1 = (String) ((Map) seats.get(0)).get("id");
        String seatId2 = (String) ((Map) seats.get(1)).get("id");

        ApplicationUser customer = new ApplicationUser(
                UUID.randomUUID(), "Cust", customerEmail, passwordEncoder.encode("password"), UserRole.CUSTOMER, Instant.now());
        userRepository.save(customer);

        ResponseEntity<Map> loginCust = rest.postForEntity(
                "/api/auth/login", Map.of("email", customerEmail, "password", "password"), Map.class);
        String custToken = (String) loginCust.getBody().get("token");

        HttpHeaders custHeaders = new HttpHeaders();
        custHeaders.setBearerAuth(custToken);
        custHeaders.setContentType(MediaType.APPLICATION_JSON);

        ReserveSeatsRequest reserveReq = new ReserveSeatsRequest(
                eventId, List.of(UUID.fromString(seatId1), UUID.fromString(seatId2)));
        ResponseEntity<Map> reserveResp = rest.exchange(
                "/api/reservations", HttpMethod.POST, new HttpEntity<>(reserveReq, custHeaders), Map.class);
        assertThat(reserveResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(reserveResp.getBody().get("status")).isEqualTo("PENDING");
        UUID reservationId = UUID.fromString((String) reserveResp.getBody().get("id"));

        ResponseEntity<Map> payResp = rest.exchange(
                "/api/reservations/" + reservationId + "/pay",
                HttpMethod.POST,
                new HttpEntity<>(null, custHeaders),
                Map.class);
        assertThat(payResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(payResp.getBody().get("status")).isEqualTo("PAID");
        List tickets = (List) payResp.getBody().get("tickets");
        assertThat(tickets).hasSize(2);
    }
}
