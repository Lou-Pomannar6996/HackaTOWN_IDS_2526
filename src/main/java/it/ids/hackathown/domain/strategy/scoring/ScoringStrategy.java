package it.ids.hackathown.domain.strategy.scoring;

import it.ids.hackathown.domain.enums.ScoringPolicyType;
import java.math.BigDecimal;

public interface ScoringStrategy {

    ScoringPolicyType policyType();

    BigDecimal computeScore(ScoringInput input);
}
