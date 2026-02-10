package it.ids.hackathown.api.dto.response;

import it.ids.hackathown.domain.enums.InviteStatus;
import java.time.LocalDateTime;

public record InviteResponse(
    Long id,
    Long teamId,
    String invitedEmail,
    InviteStatus status,
    LocalDateTime createdAt
) {
}
