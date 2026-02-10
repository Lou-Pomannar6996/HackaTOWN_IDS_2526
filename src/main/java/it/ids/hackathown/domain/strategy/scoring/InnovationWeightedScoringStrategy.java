package it.ids.hackathown.domain.strategy.scoring;

import it.ids.hackathown.domain.enums.ScoringPolicyType;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class InnovationWeightedScoringStrategy extends AbstractScoringStrategy {

    private static final BigDecimal RAW_WEIGHT = new BigDecimal("0.40");
    private static final BigDecimal INNOVATION_WEIGHT = new BigDecimal("0.40");
    private static final BigDecimal TECHNICAL_WEIGHT = new BigDecimal("0.20");

    @Override
    public ScoringPolicyType policyType() {
        return ScoringPolicyType.INNOVATION_WEIGHTED;
    }

    @Override
    public BigDecimal computeScore(ScoringInput input) {
        BigDecimal raw = normalize(input.judgeScore());
        BigDecimal innovation = normalize(defaultIfNull(input.innovationScore(), raw));
        BigDecimal technical = normalize(defaultIfNull(input.technicalScore(), raw));

        BigDecimal weighted = raw.multiply(RAW_WEIGHT)
            .add(innovation.multiply(INNOVATION_WEIGHT))
            .add(technical.multiply(TECHNICAL_WEIGHT));

        return normalize(weighted);
    }
}
