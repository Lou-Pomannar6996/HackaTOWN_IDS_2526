package it.ids.hackathown.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EvaluateSubmissionRequest(
    @NotNull @Min(0) @Max(10) Integer punteggio,
    @NotBlank String giudizio
) {
}
