package br.com.ticketsexpress.tickets_express_api.ticket;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record TransferTicketRequest(
        @NotBlank @Email String email
) {
}
