package br.com.ticketsexpress.tickets_express_api.ticket;

import br.com.ticketsexpress.tickets_express_api.shared.ClockProvider;
import br.com.ticketsexpress.tickets_express_api.shared.UuidProvider;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class TicketCodeService {

    private final ClockProvider clockProvider;
    private final UuidProvider uuidProvider;

    public TicketCodeService(ClockProvider clockProvider, UuidProvider uuidProvider) {
        this.clockProvider = clockProvider;
        this.uuidProvider = uuidProvider;
    }

    public String generateCode() {
        String seed = clockProvider.now().toString() + ":" + uuidProvider.nextId();
        return sha256(seed);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
