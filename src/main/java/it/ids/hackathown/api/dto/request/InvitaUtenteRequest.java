package it.ids.hackathown.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InvitaUtenteRequest(
    @NotNull @Min(1) Integer destinatarioId,
    @NotNull @Min(1) Integer teamId
) {
}
