package br.com.ticketsexpress.tickets_express_api.auth;

import java.util.UUID;

public record LoginResponse(String token, UUID id, String name, String email, UserRole role) {
}
