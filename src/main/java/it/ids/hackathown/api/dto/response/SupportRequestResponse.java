package it.ids.hackathown.api.dto.response;

import it.ids.hackathown.domain.enums.StatoRichiesta;
import java.time.LocalDateTime;

public record SupportRequestResponse(
    Integer id,
    Integer hackathonId,
    Integer teamId,
    String message,
    LocalDateTime createdAt,
    StatoRichiesta status
) {
}
