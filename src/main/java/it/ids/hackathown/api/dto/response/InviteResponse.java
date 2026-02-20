package it.ids.hackathown.api.dto.response;

import it.ids.hackathown.domain.enums.StatoInvito;
import java.time.LocalDateTime;

public record InviteResponse(
    Long id,
    Long teamId,
    String invitedEmail,
    StatoInvito status,
    LocalDateTime createdAt
) {
}
