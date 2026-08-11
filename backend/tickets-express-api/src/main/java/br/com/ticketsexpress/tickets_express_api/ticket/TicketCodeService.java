package br.com.ticketsexpress.tickets_express_api.ticket;

import br.com.ticketsexpress.tickets_express_api.config.AppProperties;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Service
public class TicketCodeService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final AppProperties properties;

    public TicketCodeService(AppProperties properties) {
        this.properties = properties;
    }

    public String hashCode(String rawCode) {
        return hmacHex(properties.qrSecret(), rawCode);
    }

    public String hashShareToken(String rawToken) {
        return hmacHex(properties.qrSecret(), rawToken);
    }

    private String hmacHex(String secret, String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] digest = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate secure hash", ex);
        }
    }
}
