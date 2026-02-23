package it.ids.hackathown.api.dto.response;

import java.util.Date;

public record SubmissionResponse(
    Integer id,
    Integer registrationId,
    String title,
    String description,
    String repoUrl,
    Date updatedAt,
    Date submittedAt
) {
}
