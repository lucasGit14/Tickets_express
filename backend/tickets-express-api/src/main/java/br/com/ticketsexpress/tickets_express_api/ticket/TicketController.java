package br.com.ticketsexpress.tickets_express_api.ticket;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<TicketResponse>> me() {
        return ResponseEntity.ok(ticketService.listMine());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ticketService.getById(id));
    }

    @PostMapping("/validate")
    public ResponseEntity<TicketResponse> validate(@Valid @RequestBody ValidateTicketRequest request) {
        return ResponseEntity.ok(ticketService.validate(request));
    }

    @PostMapping("/{id}/transfer")
    public ResponseEntity<TicketResponse> transfer(@PathVariable UUID id,
                                                   @Valid @RequestBody TransferTicketRequest request) {
        return ResponseEntity.ok(ticketService.transfer(id, request));
    }

    @PostMapping("/purchase/{eventId}")
    public ResponseEntity<?> purchase(@PathVariable UUID eventId, @RequestBody PurchaseRequest request) {
        return ResponseEntity.ok(ticketService.purchase(eventId, request));
    }
}
