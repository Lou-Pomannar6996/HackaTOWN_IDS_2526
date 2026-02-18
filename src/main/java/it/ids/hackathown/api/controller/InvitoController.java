package it.ids.hackathown.api.controller;

import it.ids.hackathown.api.dto.response.InviteResponse;
import it.ids.hackathown.api.mapper.ApiMapper;
import it.ids.hackathown.service.InvitoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invites")
@RequiredArgsConstructor
public class InvitoController {

    private final InvitoService invitoService;
    private final ApiMapper mapper;

    @PostMapping("/{inviteId}/accept")
    public InviteResponse accettaInvito(
        @PathVariable Long inviteId,
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId
    ) {
        return mapper.toResponse(invitoService.accettaInvito(inviteId, currentUserId));
    }

    public InviteResponse invitaUtente(Long teamId, Long currentUserId, String email) {
        return mapper.toResponse(invitoService.invitaUtenteATeam(teamId, currentUserId, email));
    }

    public List<InviteResponse> getInvitiUtente(Long currentUserId) {
        return invitoService.invitiPendentiPerUtente(currentUserId).stream().map(mapper::toResponse).toList();
    }
}
