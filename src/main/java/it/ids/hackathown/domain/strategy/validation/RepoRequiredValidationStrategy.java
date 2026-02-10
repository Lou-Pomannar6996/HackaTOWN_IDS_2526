package it.ids.hackathown.domain.strategy.validation;

import it.ids.hackathown.domain.enums.ValidationPolicyType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RepoRequiredValidationStrategy implements SubmissionValidationStrategy {

    @Override
    public ValidationPolicyType policyType() {
        return ValidationPolicyType.REPO_REQUIRED;
    }

    @Override
    public SubmissionValidationResult validate(SubmissionValidationInput input) {
        List<String> errors = new ArrayList<>();

        if (isBlank(input.repoUrl())) {
            errors.add("Repository URL is required by this hackathon");
        }
        if (!isBlank(input.repoUrl()) && !(input.repoUrl().startsWith("http://") || input.repoUrl().startsWith("https://"))) {
            errors.add("Repository URL must start with http:// or https://");
        }
        if (isBlank(input.description()) || input.description().trim().length() < 30) {
            errors.add("Description must contain at least 30 characters");
        }

        return errors.isEmpty() ? SubmissionValidationResult.ok() : SubmissionValidationResult.withErrors(errors);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
