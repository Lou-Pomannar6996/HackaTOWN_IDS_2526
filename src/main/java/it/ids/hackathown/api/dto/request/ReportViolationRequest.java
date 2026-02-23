package it.ids.hackathown.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReportViolationRequest(
    @NotBlank String descrizione,
    @NotBlank String motivazione
) {
}
