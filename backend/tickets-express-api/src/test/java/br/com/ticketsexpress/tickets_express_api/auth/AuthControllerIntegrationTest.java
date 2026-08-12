package br.com.ticketsexpress.tickets_express_api.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
public class AuthControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void registerAndLoginFlow_shouldReturnTokenAndAllowLogin() {
        String email = "test-" + UUID.randomUUID() + "@example.com";
        Map<String, Object> register = new java.util.HashMap<>();
        register.put("name", "Test User");
        register.put("email", email);
        register.put("password", "password123");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(register, headers);

        ResponseEntity<String> registerResp = restTemplate.postForEntity("/api/auth/register", req, String.class);
        assertThat(registerResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Map<String, String> login = Map.of(
                "email", email,
                "password", "password123"
        );
        HttpEntity<Map<String, String>> loginReq = new HttpEntity<>(login, headers);
        ResponseEntity<String> loginResp = restTemplate.postForEntity("/api/auth/login", loginReq, String.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResp.getBody()).isNotNull();

        // invalid credentials
        Map<String, String> bad = Map.of("email", email, "password", "wrong");
        ResponseEntity<String> badResp = restTemplate.postForEntity("/api/auth/login", new HttpEntity<>(bad, headers), String.class);
        assertThat(badResp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
