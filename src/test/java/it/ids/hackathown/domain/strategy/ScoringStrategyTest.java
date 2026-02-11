package it.ids.hackathown.domain.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import it.ids.hackathown.domain.strategy.scoring.InnovationWeightedScoringStrategy;
import it.ids.hackathown.domain.strategy.scoring.ScoringInput;
import it.ids.hackathown.domain.strategy.scoring.TechnicalWeightedScoringStrategy;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ScoringStrategyTest {

    @Test
    void innovationWeightedStrategy_computesExpectedScore() {
        InnovationWeightedScoringStrategy strategy = new InnovationWeightedScoringStrategy();

        BigDecimal result = strategy.computeScore(new ScoringInput(
            new BigDecimal("8.0"),
            new BigDecimal("10.0"),
            new BigDecimal("6.0")
        ));

        assertEquals(new BigDecimal("8.40"), result);
    }

    @Test
    void technicalWeightedStrategy_computesExpectedScore() {
        TechnicalWeightedScoringStrategy strategy = new TechnicalWeightedScoringStrategy();

        BigDecimal result = strategy.computeScore(new ScoringInput(
            new BigDecimal("8.0"),
            new BigDecimal("10.0"),
            new BigDecimal("6.0")
        ));

        assertEquals(new BigDecimal("7.60"), result);
    }
}
