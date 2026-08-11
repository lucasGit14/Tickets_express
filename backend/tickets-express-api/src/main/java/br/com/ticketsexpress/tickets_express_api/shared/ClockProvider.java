package br.com.ticketsexpress.tickets_express_api.shared;

import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class ClockProvider {

    private final Clock clock = Clock.systemUTC();

    public Clock clock() {
        return clock;
    }
}
