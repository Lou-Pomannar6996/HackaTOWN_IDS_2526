package it.ids.hackathown.api.controller;

import it.ids.hackathown.api.dto.request.CreateTeamRequest;
import it.ids.hackathown.api.dto.response.TeamResponse;
import it.ids.hackathown.api.mapper.ApiMapper;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        @RequestHeader(HeaderConstants.USER_ID) Integer currentUserId,
        @Valid @RequestBody CreateTeamRequest request
    ) {
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw new DomainValidationException("Nome team non valido");
        }
        TeamResponse response = mapper.toResponse(
            teamService.creaTeam(
                request.name(),
                requirePositiveId(currentUserId, "Utente"),
                request.maxSize()
            )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/leave")
    public ResponseEntity<Void> abbandonaTeam(@RequestHeader(HeaderConstants.USER_ID) Integer currentUserId) {
        teamService.abbandonaTeam(requirePositiveId(currentUserId, "Utente"));
        return ResponseEntity.ok().build();
    }

    private Integer requirePositiveId(Integer value, String label) {
        if (value == null || value <= 0) {
            throw new DomainValidationException(label + " non valido");
        }
        return value;
    }
}
