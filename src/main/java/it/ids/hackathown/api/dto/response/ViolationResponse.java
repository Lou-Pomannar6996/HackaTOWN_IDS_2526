package it.ids.hackathown.api.dto.response;

import it.ids.hackathown.domain.enums.StatoSegnalazione;
import java.time.LocalDateTime;

public record ViolationResponse(
    Long id,
    Long hackathonId,
    Long mentorId,
    String reason,
    LocalDateTime createdAt,
    StatoSegnalazione status
) {
}
