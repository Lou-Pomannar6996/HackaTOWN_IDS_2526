package it.ids.hackathown.api.dto.response;

import it.ids.hackathown.domain.enums.HackathonStateType;
import it.ids.hackathown.domain.enums.ScoringPolicyType;
import it.ids.hackathown.domain.enums.ValidationPolicyType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HackathonResponse(
    Long id,
    String name,
    String rules,
    LocalDateTime registrationDeadline,
    LocalDateTime startDate,
    LocalDateTime endDate,
    String location,
    BigDecimal prizeMoney,
    Integer maxTeamSize,
    HackathonStateType state,
    ScoringPolicyType scoringPolicyType,
    ValidationPolicyType validationPolicyType,
    Long organizerUserId,
    Long judgeUserId
) {
}
