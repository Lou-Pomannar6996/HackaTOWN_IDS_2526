package it.ids.hackathown.api.dto.request;

public record UpsertSubmissionRequest(
    String repoUrl,
    String fileRef,
    String description
) {
}
