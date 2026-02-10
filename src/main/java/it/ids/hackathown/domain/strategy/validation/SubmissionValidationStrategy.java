package it.ids.hackathown.domain.strategy.validation;

import it.ids.hackathown.domain.enums.ValidationPolicyType;

public interface SubmissionValidationStrategy {

    ValidationPolicyType policyType();

    SubmissionValidationResult validate(SubmissionValidationInput input);
}
