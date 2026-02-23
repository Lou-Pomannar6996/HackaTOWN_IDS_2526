package it.ids.hackathown.api.controller;

import it.ids.hackathown.api.dto.request.EvaluateSubmissionRequest;
import it.ids.hackathown.api.dto.response.EvaluationResponse;
import it.ids.hackathown.api.mapper.ApiMapper;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.service.SubmissionService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
public class EvaluationController {

    private final SubmissionService submissionService;
    private final ApiMapper mapper;

    @PostMapping("/submissions/{submissionId}/evaluations")
    public EvaluationResponse evaluateSubmission(
        @PathVariable Long submissionId,
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId,
        @Valid @RequestBody EvaluateSubmissionRequest request
    ) {
        return mapper.toResponse(submissionService.salvaValutazione(
            requirePositiveId(currentUserId, "Giudice"),
            requirePositiveId(submissionId, "Sottomissione"),
            request.punteggio(),
            request.giudizio()
        ));
    }

    @GetMapping("/hackathons/{hackathonId}/evaluations")
    public List<EvaluationResponse> listEvaluations(
        @PathVariable Long hackathonId,
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId
    ) {
        return submissionService.listValutazioni(
            requirePositiveId(hackathonId, "Hackathon"),
            requirePositiveId(currentUserId, "Giudice")
        )
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
