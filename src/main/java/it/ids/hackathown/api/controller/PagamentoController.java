package it.ids.hackathown.api.controller;

import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagamenti")
@RequiredArgsConstructor
public class PagamentoController {

    private final PaymentService pagamentoService;

    @PostMapping("/hackathons/{hackathonId}/prize")
    public ResponseEntity<String> erogaPremio(
        @RequestHeader(HeaderConstants.USER_ID) Integer currentUserId,
        @PathVariable Integer hackathonId
    ) {
        Integer organizzatoreId = requirePositiveId(currentUserId, "Organizzatore");
        Integer hackathon = requirePositiveId(hackathonId, "Hackathon");
        pagamentoService.erogaPremio(organizzatoreId, hackathon);
        return ResponseEntity.ok("Pagamento effettuato con successo");
    }

    private Integer requirePositiveId(Integer value, String label) {
        if (value == null || value <= 0) {
            throw new DomainValidationException(label + " non valido");
        }
        return value;
    }
}
