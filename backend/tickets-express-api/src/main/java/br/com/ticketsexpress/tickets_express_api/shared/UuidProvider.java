package br.com.ticketsexpress.tickets_express_api.shared;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidProvider {

    public UUID randomUuid() {
        return UUID.randomUUID();
    }
}
