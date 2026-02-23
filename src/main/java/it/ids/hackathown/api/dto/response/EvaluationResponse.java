package it.ids.hackathown.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EvaluationResponse(
    Integer id,
    Integer hackathonId,
    Integer submissionId,
    Integer judgeId,
    BigDecimal score0to10,
    String comment,
    LocalDateTime createdAt
) {
}
