package it.ids.hackathown.api.controller;

import it.ids.hackathown.api.dto.request.ReportViolationRequest;
import it.ids.hackathown.api.dto.response.ViolationResponse;
import it.ids.hackathown.api.mapper.ApiMapper;
import it.ids.hackathown.service.ViolationService;
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
@RequiredArgsConstructor
@RequestMapping("/api/hackathons")
public class ViolationController {

    private final ViolationService violationService;
    private final ApiMapper mapper;

    @PostMapping("/{hackathonId}/violations")
    public ResponseEntity<ViolationResponse> reportViolation(
        @PathVariable Long hackathonId,
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId,
        @Valid @RequestBody ReportViolationRequest request
    ) {
        ViolationResponse response = mapper.toResponse(
            violationService.reportViolation(hackathonId, request.teamId(), currentUserId, request.reason())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
