package it.ids.hackathown.domain.strategy.validation;

import java.util.List;

public record SubmissionValidationResult(boolean valid, List<String> errors) {

    public static SubmissionValidationResult ok() {
        return new SubmissionValidationResult(true, List.of());
    }

    public static SubmissionValidationResult withErrors(List<String> errors) {
        return new SubmissionValidationResult(false, List.copyOf(errors));
    }
}
