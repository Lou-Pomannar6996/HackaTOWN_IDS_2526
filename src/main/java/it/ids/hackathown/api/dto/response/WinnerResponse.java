package it.ids.hackathown.api.dto.response;

import java.time.LocalDateTime;

public record WinnerResponse(
    Integer hackathonId,
    Integer teamId,
    LocalDateTime declaredAt,
    String paymentTxId
) {
}
