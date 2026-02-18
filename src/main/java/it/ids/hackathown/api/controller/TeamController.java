package it.ids.hackathown.api.controller;

import it.ids.hackathown.api.dto.request.CreateTeamRequest;
import it.ids.hackathown.api.dto.request.InviteUserRequest;
import it.ids.hackathown.api.dto.response.InviteResponse;
import it.ids.hackathown.api.dto.response.TeamResponse;
import it.ids.hackathown.api.mapper.ApiMapper;
import it.ids.hackathown.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final ApiMapper mapper;

    @PostMapping
    public ResponseEntity<TeamResponse> creaTeam(
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId,
        @Valid @RequestBody CreateTeamRequest request
    ) {
        TeamResponse response = mapper.toResponse(teamService.creaTeam(currentUserId, request.name(), request.maxSize()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{teamId}/invites")
    public ResponseEntity<InviteResponse> inviteUser(
        @PathVariable Long teamId,
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId,
        @Valid @RequestBody InviteUserRequest request
    ) {
        InviteResponse response = mapper.toResponse(teamService.inviteUser(teamId, currentUserId, request.email()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{teamId}/leave")
    public TeamResponse abbandonaTeam(
        @PathVariable Long teamId,
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId
    ) {
        return mapper.toResponse(teamService.abbandonaTeam(teamId, currentUserId));
    }
}
