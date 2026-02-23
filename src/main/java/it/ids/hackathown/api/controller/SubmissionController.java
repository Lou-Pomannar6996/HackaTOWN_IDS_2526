package it.ids.hackathown.api.controller;

import it.ids.hackathown.api.dto.request.EvaluateSubmissionRequest;
import it.ids.hackathown.api.dto.request.UpsertSubmissionRequest;
import it.ids.hackathown.api.dto.response.SubmissionResponse;
import it.ids.hackathown.api.mapper.ApiMapper;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.service.SubmissionService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class SubmissionController {

    private final SubmissionService submissionService;
    private final ApiMapper mapper;

    @GetMapping("/hackathons/{hackathonId}/submissions/form")
    public SubmissionResponse getSubmissionForm(
        @PathVariable Long hackathonId,
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId
    ) {
        return mapper.toResponse(submissionService.getSubmissionForm(
            requirePositiveId(currentUserId, "Utente"),
            requirePositiveId(hackathonId, "Hackathon")
        ));
    }

    @PostMapping("/hackathons/{hackathonId}/submissions")
    public ResponseEntity<Void> salvaSubmission(
        @PathVariable Long hackathonId,
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId,
        @Valid @RequestBody UpsertSubmissionRequest request
    ) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("titolo", request.title());
        payload.put("descrizione", request.description());
        payload.put("urlRepo", request.repoUrl());
        submissionService.caricaSottomissione(
            requirePositiveId(currentUserId, "Utente"),
            requirePositiveId(hackathonId, "Hackathon"),
            payload
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/submissions/judge")
    public List<SubmissionResponse> getSottomissioniGiudice(
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId
    ) {
        return submissionService.getSottomissioni(requirePositiveId(currentUserId, "Giudice"))
            .stream()
            .map(mapper::toResponse)
            .toList();
    }

    @GetMapping("/submissions/{submissionId}")
    public SubmissionResponse getDettaglioSottomissione(
        @PathVariable Long submissionId,
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId
    ) {
        return mapper.toResponse(submissionService.getDettaglioSottomissione(
            requirePositiveId(submissionId, "Sottomissione"),
            requirePositiveId(currentUserId, "Giudice")
        ));
    }

    @PostMapping("/submissions/{submissionId}/evaluation")
    public ResponseEntity<Void> salvaValutazione(
        @PathVariable Long submissionId,
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId,
        @Valid @RequestBody EvaluateSubmissionRequest request
    ) {
        submissionService.salvaValutazione(
            requirePositiveId(currentUserId, "Giudice"),
            requirePositiveId(submissionId, "Sottomissione"),
            request.punteggio(),
            request.giudizio()
        );
        return ResponseEntity.ok().build();
    }

    private Integer requirePositiveId(Long value, String label) {
        if (value == null || value <= 0) {
            throw new DomainValidationException(label + " non valido");
        }
        return value.intValue();
    }
}
