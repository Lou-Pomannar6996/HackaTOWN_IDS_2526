package it.ids.hackathown.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AggiornaStatoRequest(@NotBlank String nuovoStato) {
}
