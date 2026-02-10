package it.ids.hackathown.domain.strategy.scoring;

import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class AbstractScoringStrategy implements ScoringStrategy {

    protected BigDecimal normalize(BigDecimal value) {
        BigDecimal safe = value == null ? BigDecimal.ZERO : value;
        if (safe.compareTo(BigDecimal.ZERO) < 0) {
            safe = BigDecimal.ZERO;
        }
        if (safe.compareTo(BigDecimal.TEN) > 0) {
            safe = BigDecimal.TEN;
        }
        return safe.setScale(2, RoundingMode.HALF_UP);
    }

    protected BigDecimal defaultIfNull(BigDecimal value, BigDecimal fallback) {
        return value == null ? fallback : value;
    }
}
