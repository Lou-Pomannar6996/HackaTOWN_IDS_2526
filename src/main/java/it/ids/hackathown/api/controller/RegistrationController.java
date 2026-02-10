package it.ids.hackathown.api.controller;

import it.ids.hackathown.api.dto.request.RegisterTeamRequest;
import it.ids.hackathown.api.dto.response.RegistrationResponse;
import it.ids.hackathown.api.mapper.ApiMapper;
import it.ids.hackathown.service.RegistrationService;
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

    private final RegistrationService registrationService;
    private final ApiMapper mapper;

    @PostMapping("/{hackathonId}/registrations")
    public ResponseEntity<RegistrationResponse> registerTeam(
        @PathVariable Long hackathonId,
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId,
        @Valid @RequestBody RegisterTeamRequest request
    ) {
        RegistrationResponse response = mapper.toResponse(
            registrationService.registerTeam(hackathonId, request.teamId(), currentUserId)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
