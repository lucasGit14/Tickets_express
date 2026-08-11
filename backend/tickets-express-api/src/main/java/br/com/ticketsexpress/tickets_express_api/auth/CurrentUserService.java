package br.com.ticketsexpress.tickets_express_api.auth;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CurrentUserService {

    public UUID requireCurrentUserId() {
        throw new UnsupportedOperationException("Authentication not implemented yet");
    }
}
