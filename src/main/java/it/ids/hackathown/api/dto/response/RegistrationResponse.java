package it.ids.hackathown.api.dto.response;

import java.time.LocalDateTime;

public record RegistrationResponse(
    Long id,
    Long hackathonId,
    Long teamId,
    LocalDateTime createdAt
) {
}
