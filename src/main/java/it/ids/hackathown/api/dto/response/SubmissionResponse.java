package it.ids.hackathown.api.dto.response;

import java.util.Date;

public record SubmissionResponse(
    Long id,
    Long registrationId,
    String title,
    String description,
    String repoUrl,
    Date updatedAt,
    Date submittedAt
) {
}
