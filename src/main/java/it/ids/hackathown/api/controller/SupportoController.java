package it.ids.hackathown.api.controller;

import it.ids.hackathown.api.dto.request.CreateSupportRequest;
import it.ids.hackathown.api.dto.request.PianificaCallRequest;
import it.ids.hackathown.api.dto.response.SupportRequestResponse;
import it.ids.hackathown.api.mapper.ApiMapper;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.service.SupportoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SupportoController {

    private final SupportoService supportoService;
    private final ApiMapper mapper;

    @PostMapping("/hackathons/{hackathonId}/support-requests")
    public ResponseEntity<Void> inviaRichiestaSupporto(
        @PathVariable Long hackathonId,
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId,
        @Valid @RequestBody CreateSupportRequest request
    ) {
        supportoService.creaRichiestaSupporto(
            requirePositiveId(currentUserId, "Utente"),
            requirePositiveId(hackathonId, "Hackathon"),
            request.message()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/hackathons/{hackathonId}/support-requests")
    public List<SupportRequestResponse> getRichieste(@PathVariable Long hackathonId) {
        return supportoService.listaRichieste(requirePositiveId(hackathonId, "Hackathon"))
            .stream()
            .map(mapper::toResponse)
            .toList();
    }

    @PostMapping("/support-requests/{supportRequestId}/propose-call")
    public ResponseEntity<String> pianificaCall(
        @PathVariable Long supportRequestId,
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId,
        @Valid @RequestBody PianificaCallRequest request
    ) {
        String calendarEventId = supportoService.pianificaCall(
            requirePositiveId(supportRequestId, "Richiesta"),
            requirePositiveId(currentUserId, "Mentore"),
            request.slotPreferiti()
        );
        if ("NESSUNO_SLOT_DISPONIBILE".equalsIgnoreCase(calendarEventId)) {
            throw new DomainValidationException("Nessuno slot disponibile");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(calendarEventId);
    }

    @GetMapping("/support-requests/mentor")
    public List<SupportRequestResponse> getRichiesteSupportoMentore(
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId
    ) {
        return supportoService.getRichiesteSupporto(requirePositiveId(currentUserId, "Mentore"))
            .stream()
            .map(mapper::toResponse)
            .toList();
    }

    private Integer requirePositiveId(Long value, String label) {
        if (value == null || value <= 0) {
            throw new DomainValidationException(label + " non valido");
        }
        return value.intValue();
    }
}
