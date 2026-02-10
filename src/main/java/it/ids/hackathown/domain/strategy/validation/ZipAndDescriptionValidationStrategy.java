package it.ids.hackathown.domain.strategy.validation;

import it.ids.hackathown.domain.enums.ValidationPolicyType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ZipAndDescriptionValidationStrategy implements SubmissionValidationStrategy {

    @Override
    public ValidationPolicyType policyType() {
        return ValidationPolicyType.ZIP_AND_DESCRIPTION;
    }

    @Override
    public SubmissionValidationResult validate(SubmissionValidationInput input) {
        List<String> errors = new ArrayList<>();

        if (isBlank(input.fileRef())) {
            errors.add("Zip/file reference is required by this hackathon");
        }
        if (isBlank(input.description()) || input.description().trim().length() < 80) {
            errors.add("Description must contain at least 80 characters");
        }

        return errors.isEmpty() ? SubmissionValidationResult.ok() : SubmissionValidationResult.withErrors(errors);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
