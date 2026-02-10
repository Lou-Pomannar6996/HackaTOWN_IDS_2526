package it.ids.hackathown.api.dto.response;

import it.ids.hackathown.domain.enums.SubmissionStatus;
import java.time.LocalDateTime;

public record SubmissionResponse(
    Long id,
    Long hackathonId,
    Long teamId,
    String repoUrl,
    String fileRef,
    String description,
    LocalDateTime updatedAt,
    SubmissionStatus status
) {
}
