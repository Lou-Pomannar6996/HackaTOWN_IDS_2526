package it.ids.hackathown.domain.strategy.validation;

import it.ids.hackathown.domain.enums.ValidationPolicyType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class BasicSubmissionValidationStrategy implements SubmissionValidationStrategy {

    @Override
    public ValidationPolicyType policyType() {
        return ValidationPolicyType.BASIC;
    }

    @Override
    public SubmissionValidationResult validate(SubmissionValidationInput input) {
        List<String> errors = new ArrayList<>();

        boolean noRepo = isBlank(input.repoUrl());
        boolean noFile = isBlank(input.fileRef());
        boolean noDescription = isBlank(input.description());

        if (noRepo && noFile && noDescription) {
            errors.add("At least one between repoUrl, fileRef or description must be provided");
        }
        if (!noDescription && input.description().trim().length() < 20) {
            errors.add("Description must contain at least 20 characters");
        }

        return errors.isEmpty() ? SubmissionValidationResult.ok() : SubmissionValidationResult.withErrors(errors);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
