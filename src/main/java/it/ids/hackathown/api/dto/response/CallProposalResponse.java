package it.ids.hackathown.api.dto.response;

import it.ids.hackathown.domain.enums.StatoCall;
import java.util.Date;

public record CallProposalResponse(
    Integer id,
    Date dataProposta,
    Date dataInizio,
    Integer durataMin,
    String calendarEventId,
    StatoCall stato
) {
}
