package it.ids.hackathown.api.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record EvaluateSubmissionRequest(
    @NotNull @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal judgeScore,
    @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal innovationScore,
    @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal technicalScore,
    @NotBlank String comment
) {
}
