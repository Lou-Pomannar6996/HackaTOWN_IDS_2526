package it.ids.hackathown.api.dto.response;

import java.time.LocalDateTime;

public record RegistrationResponse(
    Integer id,
    Integer hackathonId,
    Integer teamId,
    LocalDateTime createdAt
) {
}
