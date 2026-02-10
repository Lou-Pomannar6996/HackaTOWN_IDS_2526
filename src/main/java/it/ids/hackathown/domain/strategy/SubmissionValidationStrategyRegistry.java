package it.ids.hackathown.domain.strategy;

import it.ids.hackathown.domain.enums.ValidationPolicyType;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.domain.strategy.validation.SubmissionValidationStrategy;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SubmissionValidationStrategyRegistry {

    private final Map<ValidationPolicyType, SubmissionValidationStrategy> strategies;

    public SubmissionValidationStrategyRegistry(List<SubmissionValidationStrategy> strategyList) {
        this.strategies = new EnumMap<>(ValidationPolicyType.class);
        strategyList.forEach(strategy -> this.strategies.put(strategy.policyType(), strategy));
    }

    public SubmissionValidationStrategy getStrategy(ValidationPolicyType policyType) {
        SubmissionValidationStrategy strategy = strategies.get(policyType);
        if (strategy == null) {
            throw new DomainValidationException("Missing validation strategy for policy: " + policyType);
        }
        return strategy;
    }
}
