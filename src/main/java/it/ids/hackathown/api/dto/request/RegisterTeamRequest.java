package it.ids.hackathown.api.dto.request;

import jakarta.validation.constraints.NotNull;

public record RegisterTeamRequest(@NotNull Long teamId) {
}
