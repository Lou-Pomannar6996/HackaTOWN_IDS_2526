package it.ids.hackathown.domain.strategy;

import it.ids.hackathown.domain.enums.ScoringPolicyType;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.domain.strategy.scoring.ScoringStrategy;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ScoringStrategyRegistry {

    private final Map<ScoringPolicyType, ScoringStrategy> strategies;

    public ScoringStrategyRegistry(List<ScoringStrategy> strategyList) {
        this.strategies = new EnumMap<>(ScoringPolicyType.class);
        strategyList.forEach(strategy -> this.strategies.put(strategy.policyType(), strategy));
    }

    public ScoringStrategy getStrategy(ScoringPolicyType policyType) {
        ScoringStrategy strategy = strategies.get(policyType);
        if (strategy == null) {
            throw new DomainValidationException("Missing scoring strategy for policy: " + policyType);
        }
        return strategy;
    }
}
