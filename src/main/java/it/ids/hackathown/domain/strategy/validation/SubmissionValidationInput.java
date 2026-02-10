package it.ids.hackathown.domain.strategy.validation;

public record SubmissionValidationInput(
    String repoUrl,
    String fileRef,
    String description
) {
}
