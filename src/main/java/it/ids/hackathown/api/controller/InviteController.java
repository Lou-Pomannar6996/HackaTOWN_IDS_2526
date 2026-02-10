package it.ids.hackathown.api.controller;

import it.ids.hackathown.api.dto.response.InviteResponse;
import it.ids.hackathown.api.mapper.ApiMapper;
import it.ids.hackathown.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invites")
@RequiredArgsConstructor
public class InviteController {

    private final TeamService teamService;
    private final ApiMapper mapper;

    @PostMapping("/{inviteId}/accept")
    public InviteResponse acceptInvite(
        @PathVariable Long inviteId,
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId
    ) {
        return mapper.toResponse(teamService.acceptInvite(inviteId, currentUserId));
    }
}
