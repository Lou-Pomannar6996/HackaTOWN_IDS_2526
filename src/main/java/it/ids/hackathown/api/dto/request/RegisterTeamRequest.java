package it.ids.hackathown.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RegisterTeamRequest(@NotNull @Min(1) Integer teamId) {
}
