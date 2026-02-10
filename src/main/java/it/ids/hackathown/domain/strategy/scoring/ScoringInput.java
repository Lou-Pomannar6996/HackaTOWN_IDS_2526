package it.ids.hackathown.domain.strategy.scoring;

import java.math.BigDecimal;

public record ScoringInput(
    BigDecimal judgeScore,
    BigDecimal innovationScore,
    BigDecimal technicalScore
) {
}
