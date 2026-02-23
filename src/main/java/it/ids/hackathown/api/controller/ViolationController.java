package it.ids.hackathown.api.controller;

import it.ids.hackathown.api.dto.request.ReportViolationRequest;
import it.ids.hackathown.domain.exception.DomainValidationException;
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

    @PostMapping("/{hackathonId}/violations")
    public ResponseEntity<String> segnalaViolazione(
        @PathVariable Long hackathonId,
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId,
        @Valid @RequestBody ReportViolationRequest request
    ) {
        violationService.segnalaViolazione(
            requirePositiveId(currentUserId, "Mentore"),
            requirePositiveId(hackathonId, "Hackathon"),
            request.descrizione(),
            request.motivazione()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body("Segnalazione inviata con successo");
    }

    private Integer requirePositiveId(Long value, String label) {
        if (value == null || value <= 0) {
            throw new DomainValidationException(label + " non valido");
        }
        return value.intValue();
    }
}
