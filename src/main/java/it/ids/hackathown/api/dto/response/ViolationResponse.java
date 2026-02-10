package it.ids.hackathown.api.dto.response;

import java.time.LocalDateTime;

public record ViolationResponse(
    Long id,
    Long hackathonId,
    Long teamId,
    Long mentorId,
    String reason,
    LocalDateTime createdAt
) {
}
