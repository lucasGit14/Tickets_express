package br.com.ticketsexpress.tickets_express_api.integration;

import br.com.ticketsexpress.tickets_express_api.auth.ApplicationUser;
import br.com.ticketsexpress.tickets_express_api.auth.UserRepository;
import br.com.ticketsexpress.tickets_express_api.auth.UserRole;
import br.com.ticketsexpress.tickets_express_api.event.dto.CreateEventRequest;
import br.com.ticketsexpress.tickets_express_api.event.dto.CreateSeatRequest;
import br.com.ticketsexpress.tickets_express_api.reservation.Reservation;
import br.com.ticketsexpress.tickets_express_api.reservation.ReservationRepository;
import br.com.ticketsexpress.tickets_express_api.reservation.ReservationService;
import br.com.ticketsexpress.tickets_express_api.reservation.ReservationStatus;
import br.com.ticketsexpress.tickets_express_api.reservation.ReserveSeatsRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class FullDomainIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationService reservationService;

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void authMeAuthorizationReservationsTicketsAndTransfers() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String organizerEmail = "org-" + suffix + "@example.com";
        String otherOrgEmail = "org2-" + suffix + "@example.com";
        String customerEmail = "cust-" + suffix + "@example.com";
        String otherCustomerEmail = "cust2-" + suffix + "@example.com";
        String gatekeeperEmail = "gate-" + suffix + "@example.com";

        saveUser("Organizer", organizerEmail, UserRole.ORGANIZER);
        saveUser("Other Org", otherOrgEmail, UserRole.ORGANIZER);
        saveUser("Customer", customerEmail, UserRole.CUSTOMER);
        saveUser("Customer Two", otherCustomerEmail, UserRole.CUSTOMER);
        saveUser("Gatekeeper", gatekeeperEmail, UserRole.GATEKEEPER);

        Map orgLogin = login(organizerEmail, "password");
        String orgToken = (String) orgLogin.get("token");
        assertThat(orgLogin.get("email")).isEqualTo(organizerEmail);
        assertThat(orgLogin.get("role")).isEqualTo("ORGANIZER");
        assertThat(orgLogin.get("id")).isNotNull();
        assertThat(orgLogin.get("name")).isEqualTo("Organizer");

        HttpHeaders orgHeaders = bearer(orgToken);
        ResponseEntity<Map> meResp = rest.exchange("/api/auth/me", HttpMethod.GET, new HttpEntity<>(orgHeaders), Map.class);
        assertThat(meResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(meResp.getBody().get("email")).isEqualTo(organizerEmail);

        ResponseEntity<String> meUnauthorized = rest.getForEntity("/api/auth/me", String.class);
        assertThat(meUnauthorized.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        CreateEventRequest createEvent = new CreateEventRequest(
                99L, "Auth Event", null, "Synopsis", Instant.now().plusSeconds(7200),
                "Venue", "Street 1", new BigDecimal("25.00"), null);
        ResponseEntity<Map> eventResp = rest.exchange(
                "/api/events", HttpMethod.POST, new HttpEntity<>(createEvent, orgHeaders), Map.class);
        assertThat(eventResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID eventId = UUID.fromString((String) eventResp.getBody().get("id"));
        assertThat(eventResp.getBody().get("status")).isEqualTo("DRAFT");

        ResponseEntity<Map> publicDraft = rest.getForEntity("/api/events/" + eventId, Map.class);
        assertThat(publicDraft.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        String otherOrgToken = (String) login(otherOrgEmail, "password").get("token");
        HttpHeaders otherOrgHeaders = bearer(otherOrgToken);
        ResponseEntity<Map> otherSeesDraft = rest.exchange(
                "/api/events/" + eventId, HttpMethod.GET, new HttpEntity<>(otherOrgHeaders), Map.class);
        assertThat(otherSeesDraft.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<Map> otherUpdate = rest.exchange(
                "/api/events/" + eventId,
                HttpMethod.PUT,
                new HttpEntity<>(Map.of("title", "Hacked"), otherOrgHeaders),
                Map.class);
        assertThat(otherUpdate.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<Map> publish = rest.exchange(
                "/api/events/" + eventId + "/publish", HttpMethod.POST, new HttpEntity<>(null, orgHeaders), Map.class);
        assertThat(publish.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<List> seatsCreate = rest.exchange(
                "/api/events/" + eventId + "/seats",
                HttpMethod.POST,
                new HttpEntity<>(List.of(new CreateSeatRequest("B", 1, null), new CreateSeatRequest("B", 2, null)), orgHeaders),
                List.class);
        assertThat(seatsCreate.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String seatId = (String) ((Map) seatsCreate.getBody().get(0)).get("id");
        String seatId2 = (String) ((Map) seatsCreate.getBody().get(1)).get("id");

        String custToken = (String) login(customerEmail, "password").get("token");
        HttpHeaders custHeaders = bearer(custToken);

        ResponseEntity<Map> organizerReserve = rest.exchange(
                "/api/reservations",
                HttpMethod.POST,
                new HttpEntity<>(new ReserveSeatsRequest(eventId, List.of(UUID.fromString(seatId))), orgHeaders),
                Map.class);
        assertThat(organizerReserve.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<Map> reserve = rest.exchange(
                "/api/reservations",
                HttpMethod.POST,
                new HttpEntity<>(new ReserveSeatsRequest(eventId, List.of(UUID.fromString(seatId))), custHeaders),
                Map.class);
        assertThat(reserve.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(reserve.getBody().get("status")).isEqualTo("PENDING");
        UUID reservationId = UUID.fromString((String) reserve.getBody().get("id"));

        ResponseEntity<Map> cancel = rest.exchange(
                "/api/reservations/" + reservationId + "/cancel",
                HttpMethod.POST,
                new HttpEntity<>(null, custHeaders),
                Map.class);
        assertThat(cancel.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cancel.getBody().get("status")).isEqualTo("CANCELLED");

        ResponseEntity<Map> reserveAgain = rest.exchange(
                "/api/reservations",
                HttpMethod.POST,
                new HttpEntity<>(new ReserveSeatsRequest(eventId, List.of(UUID.fromString(seatId))), custHeaders),
                Map.class);
        assertThat(reserveAgain.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID pendingId = UUID.fromString((String) reserveAgain.getBody().get("id"));

        Reservation pending = reservationRepository.findById(pendingId).orElseThrow();
        reservationRepository.save(new Reservation(
                pending.getId(),
                pending.getCustomer(),
                pending.getEvent(),
                pending.getStatus(),
                Instant.now().minusSeconds(60),
                pending.getTotalAmount(),
                pending.getPaymentReference(),
                pending.getCreatedAt()
        ));
        reservationService.expirePendingReservations();
        assertThat(reservationRepository.findById(pendingId).orElseThrow().getStatus()).isEqualTo(ReservationStatus.EXPIRED);

        ResponseEntity<Map> reserveAfterExpire = rest.exchange(
                "/api/reservations",
                HttpMethod.POST,
                new HttpEntity<>(new ReserveSeatsRequest(eventId, List.of(UUID.fromString(seatId))), custHeaders),
                Map.class);
        assertThat(reserveAfterExpire.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID paidReservationId = UUID.fromString((String) reserveAfterExpire.getBody().get("id"));

        ResponseEntity<Map> pay = rest.exchange(
                "/api/reservations/" + paidReservationId + "/pay",
                HttpMethod.POST,
                new HttpEntity<>(null, custHeaders),
                Map.class);
        assertThat(pay.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pay.getBody().get("status")).isEqualTo("PAID");
        List tickets = (List) pay.getBody().get("tickets");
        assertThat(tickets).hasSize(1);
        String ticketId = (String) ((Map) tickets.get(0)).get("id");
        String ticketCode = (String) ((Map) tickets.get(0)).get("code");

        ResponseEntity<List> myTickets = rest.exchange(
                "/api/tickets/me", HttpMethod.GET, new HttpEntity<>(custHeaders), List.class);
        assertThat(myTickets.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(myTickets.getBody()).isNotEmpty();

        String gateToken = (String) login(gatekeeperEmail, "password").get("token");
        HttpHeaders gateHeaders = bearer(gateToken);
        ResponseEntity<Map> validate = rest.exchange(
                "/api/tickets/validate",
                HttpMethod.POST,
                new HttpEntity<>(Map.of("code", ticketCode), gateHeaders),
                Map.class);
        assertThat(validate.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(validate.getBody().get("status")).isEqualTo("USED");

        ResponseEntity<Map> validateAgain = rest.exchange(
                "/api/tickets/validate",
                HttpMethod.POST,
                new HttpEntity<>(Map.of("code", ticketCode), gateHeaders),
                Map.class);
        assertThat(validateAgain.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ResponseEntity<Map> reserve2 = rest.exchange(
                "/api/reservations",
                HttpMethod.POST,
                new HttpEntity<>(new ReserveSeatsRequest(eventId, List.of(UUID.fromString(seatId2))), custHeaders),
                Map.class);
        UUID reservation2 = UUID.fromString((String) reserve2.getBody().get("id"));
        ResponseEntity<Map> pay2 = rest.exchange(
                "/api/reservations/" + reservation2 + "/pay",
                HttpMethod.POST,
                new HttpEntity<>(null, custHeaders),
                Map.class);
        String transferableTicketId = (String) ((Map) ((List) pay2.getBody().get("tickets")).get(0)).get("id");

        ResponseEntity<Map> transfer = rest.exchange(
                "/api/tickets/" + transferableTicketId + "/transfer",
                HttpMethod.POST,
                new HttpEntity<>(Map.of("email", otherCustomerEmail), custHeaders),
                Map.class);
        assertThat(transfer.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(transfer.getBody().get("ownerEmail")).isEqualTo(otherCustomerEmail);

        ResponseEntity<List> mineEvents = rest.exchange(
                "/api/events/mine", HttpMethod.GET, new HttpEntity<>(orgHeaders), List.class);
        assertThat(mineEvents.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mineEvents.getBody()).isNotEmpty();

        ResponseEntity<Map> cancelEvent = rest.exchange(
                "/api/events/" + eventId + "/cancel", HttpMethod.POST, new HttpEntity<>(null, orgHeaders), Map.class);
        assertThat(cancelEvent.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cancelEvent.getBody().get("status")).isEqualTo("CANCELLED");
    }

    @Test
    void invalidLoginReturns401() {
        ResponseEntity<String> bad = rest.postForEntity(
                "/api/auth/login",
                Map.of("email", "missing-" + UUID.randomUUID() + "@example.com", "password", "x"),
                String.class);
        assertThat(bad.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void registerReturnsProfile() {
        String email = "reg-" + UUID.randomUUID() + "@example.com";
        Map<String, Object> body = new HashMap<>();
        body.put("name", "New User");
        body.put("email", email);
        body.put("password", "password123");
        body.put("role", "CUSTOMER");

        ResponseEntity<Map> resp = rest.postForEntity("/api/auth/register", body, Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().get("token")).isNotNull();
        assertThat(resp.getBody().get("email")).isEqualTo(email);
        assertThat(resp.getBody().get("role")).isEqualTo("CUSTOMER");
    }

    private ApplicationUser saveUser(String name, String email, UserRole role) {
        ApplicationUser user = new ApplicationUser(
                UUID.randomUUID(), name, email, passwordEncoder.encode("password"), role, Instant.now());
        return userRepository.save(user);
    }

    @SuppressWarnings("rawtypes")
    private Map login(String email, String password) {
        ResponseEntity<Map> resp = rest.postForEntity(
                "/api/auth/login", Map.of("email", email, "password", password), Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        return resp.getBody();
    }

    private HttpHeaders bearer(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
