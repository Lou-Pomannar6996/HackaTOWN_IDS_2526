package it.ids.hackathown.api.dto.response;

import java.time.LocalDateTime;

public record WinnerResponse(
    Long hackathonId,
    Long teamId,
    LocalDateTime declaredAt,
    String paymentTxId
) {
}
