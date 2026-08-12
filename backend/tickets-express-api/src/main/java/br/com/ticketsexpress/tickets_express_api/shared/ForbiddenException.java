package br.com.ticketsexpress.tickets_express_api.shared;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
