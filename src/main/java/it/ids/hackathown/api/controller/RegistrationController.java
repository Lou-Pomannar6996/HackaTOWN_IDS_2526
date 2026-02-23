package it.ids.hackathown.api.controller;

import it.ids.hackathown.api.dto.request.RegisterTeamRequest;
import it.ids.hackathown.api.dto.response.RegistrationResponse;
import it.ids.hackathown.api.mapper.ApiMapper;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.service.HackathonService;
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
@RequestMapping("/api/hackathons")
@RequiredArgsConstructor
public class RegistrationController {

    private final HackathonService hackathonService;
    private final ApiMapper mapper;

    @PostMapping("/{hackathonId}/registrations")
    public ResponseEntity<RegistrationResponse> registerTeam(
        @PathVariable Integer hackathonId,
        @RequestHeader(HeaderConstants.USER_ID) Integer currentUserId,
        @Valid @RequestBody RegisterTeamRequest request
    ) {
        if (currentUserId == null || currentUserId <= 0) {
            throw new DomainValidationException("Utente non valido");
        }
        RegistrationResponse response = mapper.toResponse(
            hackathonService.iscriviTeam(
                requirePositiveId(hackathonId, "Hackathon"),
                requirePositiveId(request.teamId(), "Team")
            )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private Integer requirePositiveId(Integer value, String label) {
        if (value == null || value <= 0) {
            throw new DomainValidationException(label + " non valido");
        }
        return value;
    }
}
