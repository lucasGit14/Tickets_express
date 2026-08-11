package br.com.ticketsexpress.tickets_express_api.auth;

import br.com.ticketsexpress.tickets_express_api.config.AppProperties;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final long expirationSeconds = 60 * 60 * 24; // 24h

    public JwtService(AppProperties properties) {
        this.algorithm = Algorithm.HMAC256(properties.jwtSecret());
        this.verifier = JWT.require(algorithm).build();
    }

    public String generateToken(ApplicationUser user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expirationSeconds);
        return JWT.create()
                .withSubject(user.getEmail())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(exp))
                .sign(algorithm);
    }

    public String getSubject(String token) {
        try {
            DecodedJWT decoded = verifier.verify(token);
            return decoded.getSubject();
        } catch (JWTVerificationException ex) {
            throw new IllegalArgumentException("Invalid token", ex);
        }
    }

    public boolean validate(String token) {
        try {
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException ex) {
            return false;
        }
    }
}
