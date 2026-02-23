package it.ids.hackathown.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpsertSubmissionRequest(
    @NotBlank String title,
    String description,
    @NotBlank String repoUrl
) {
}
