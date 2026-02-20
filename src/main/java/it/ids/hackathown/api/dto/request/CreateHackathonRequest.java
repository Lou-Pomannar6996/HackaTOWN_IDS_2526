package it.ids.hackathown.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

public record CreateHackathonRequest(
    @NotBlank String name,
    String description,
    String rules,
    @NotNull Date registrationDeadline,
    @NotNull Date startDate,
    @NotNull Date endDate,
    String location,
    @NotNull @DecimalMin("0.0") BigDecimal prizeMoney,
    @NotNull @Min(1) Integer maxTeamSize
) {
}
