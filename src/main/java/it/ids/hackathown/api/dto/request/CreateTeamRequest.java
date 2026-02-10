package it.ids.hackathown.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTeamRequest(
    @NotBlank String name,
    @NotNull @Min(1) Integer maxSize
) {
}
