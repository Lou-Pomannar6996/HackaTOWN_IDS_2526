package it.ids.hackathown.api.dto.request;

public record UpsertSubmissionRequest(
    String title,
    String description,
    String repoUrl
) {
}
