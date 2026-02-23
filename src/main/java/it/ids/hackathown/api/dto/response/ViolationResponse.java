package it.ids.hackathown.api.dto.response;

import it.ids.hackathown.domain.enums.StatoSegnalazione;
import java.time.LocalDateTime;

public record ViolationResponse(
    Integer id,
    Integer hackathonId,
    Integer mentorId,
    String descrizione,
    String motivazione,
    LocalDateTime createdAt,
    StatoSegnalazione status
) {
}
