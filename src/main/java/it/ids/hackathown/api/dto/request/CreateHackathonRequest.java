package it.ids.hackathown.api.dto.request;

import it.ids.hackathown.domain.enums.ScoringPolicyType;
import it.ids.hackathown.domain.enums.ValidationPolicyType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CreateHackathonRequest(
    @NotBlank String name,
    String rules,
    @NotNull LocalDateTime registrationDeadline,
    @NotNull LocalDateTime startDate,
    @NotNull LocalDateTime endDate,
    String location,
    @NotNull @DecimalMin("0.0") BigDecimal prizeMoney,
    @NotNull @Min(1) Integer maxTeamSize,
    @NotNull Long judgeUserId,
    List<Long> mentorUserIds,
    @NotNull ScoringPolicyType scoringPolicyType,
    @NotNull ValidationPolicyType validationPolicyType
) {
}
