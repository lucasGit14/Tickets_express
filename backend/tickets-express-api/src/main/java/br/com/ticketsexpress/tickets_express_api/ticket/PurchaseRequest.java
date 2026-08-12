package br.com.ticketsexpress.tickets_express_api.ticket;

import jakarta.validation.constraints.NotBlank;

public record PurchaseRequest(@NotBlank String paymentMethod) {
}
