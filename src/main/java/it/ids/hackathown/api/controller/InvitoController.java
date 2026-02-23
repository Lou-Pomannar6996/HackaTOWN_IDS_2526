package it.ids.hackathown.api.controller;

import it.ids.hackathown.api.dto.request.InvitaUtenteRequest;
import it.ids.hackathown.api.dto.response.InviteResponse;
import it.ids.hackathown.api.mapper.ApiMapper;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.service.InvitoService;
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
@RequestMapping("/api/invites")
@RequiredArgsConstructor
public class InvitoController {

    private final InvitoService invitoService;
    private final ApiMapper mapper;

    @PostMapping
    public ResponseEntity<Void> invitaUtente(
        @RequestHeader(HeaderConstants.USER_ID) Integer currentUserId,
        @Valid @RequestBody InvitaUtenteRequest request
    ) {
        invitoService.invitaUtenteATeam(
            requirePositiveId(currentUserId, "Mittente"),
            requirePositiveId(request.destinatarioId(), "Destinatario"),
            requirePositiveId(request.teamId(), "Team")
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{inviteId}/accept")
    public ResponseEntity<Void> accettaInvito(
        @PathVariable Integer inviteId,
        @RequestHeader(HeaderConstants.USER_ID) Integer currentUserId
    ) {
        invitoService.accettaInvito(
            requirePositiveId(inviteId, "Invito"),
            requirePositiveId(currentUserId, "Utente")
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<InviteResponse> getInvitiUtente(@RequestHeader(HeaderConstants.USER_ID) Integer currentUserId) {
        return invitoService.getInviti(requirePositiveId(currentUserId, "Utente"))
            .stream()
            .map(mapper::toResponse)
            .toList();
    }

    private Integer requirePositiveId(Integer value, String label) {
        if (value == null || value <= 0) {
            throw new DomainValidationException(label + " non valido");
        }
        return value;
    }
}
