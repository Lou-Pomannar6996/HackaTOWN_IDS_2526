package it.ids.hackathown.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EvaluationResponse(
    Long id,
    Long hackathonId,
    Long submissionId,
    Long judgeId,
    BigDecimal score0to10,
    String comment,
    LocalDateTime createdAt
) {
}
