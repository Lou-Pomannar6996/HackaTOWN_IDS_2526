package it.ids.hackathown.domain.strategy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.ids.hackathown.domain.strategy.validation.RepoRequiredValidationStrategy;
import it.ids.hackathown.domain.strategy.validation.SubmissionValidationInput;
import it.ids.hackathown.domain.strategy.validation.SubmissionValidationResult;
import it.ids.hackathown.domain.strategy.validation.ZipAndDescriptionValidationStrategy;
import org.junit.jupiter.api.Test;

class SubmissionValidationStrategyTest {

    @Test
    void repoRequiredStrategy_rejectsMissingRepo() {
        RepoRequiredValidationStrategy strategy = new RepoRequiredValidationStrategy();

        SubmissionValidationResult result = strategy.validate(new SubmissionValidationInput(
            null,
            "artifact.zip",
            "This description is long enough but repo is missing"
        ));

        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(error -> error.contains("Repository URL is required")));
    }

    @Test
    void repoRequiredStrategy_acceptsValidPayload() {
        RepoRequiredValidationStrategy strategy = new RepoRequiredValidationStrategy();

        SubmissionValidationResult result = strategy.validate(new SubmissionValidationInput(
            "https://github.com/hackhub/project",
            null,
            "Detailed project description with at least thirty characters."
        ));

        assertTrue(result.valid());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void zipAndDescriptionStrategy_rejectsShortDescription() {
        ZipAndDescriptionValidationStrategy strategy = new ZipAndDescriptionValidationStrategy();

        SubmissionValidationResult result = strategy.validate(new SubmissionValidationInput(
            "https://github.com/hackhub/project",
            "submission.zip",
            "Too short"
        ));

        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(error -> error.contains("at least 80")));
    }

    @Test
    void zipAndDescriptionStrategy_acceptsValidPayload() {
        ZipAndDescriptionValidationStrategy strategy = new ZipAndDescriptionValidationStrategy();

        SubmissionValidationResult result = strategy.validate(new SubmissionValidationInput(
            null,
            "submission.zip",
            "This is a sufficiently long description that satisfies the minimum length requirement configured in the strategy."
        ));

        assertTrue(result.valid());
        assertTrue(result.errors().isEmpty());
    }
}
