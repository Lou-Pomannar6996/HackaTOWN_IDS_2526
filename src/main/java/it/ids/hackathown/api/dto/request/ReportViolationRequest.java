package it.ids.hackathown.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReportViolationRequest(
    @NotNull Long teamId,
    @NotBlank String reason
) {
}
