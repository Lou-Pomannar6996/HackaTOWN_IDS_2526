package it.ids.hackathown.domain.strategy.scoring;

import it.ids.hackathown.domain.enums.ScoringPolicyType;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class DefaultScoringStrategy extends AbstractScoringStrategy {

    @Override
    public ScoringPolicyType policyType() {
        return ScoringPolicyType.DEFAULT;
    }

    @Override
    public BigDecimal computeScore(ScoringInput input) {
        return normalize(input.judgeScore());
    }
}
