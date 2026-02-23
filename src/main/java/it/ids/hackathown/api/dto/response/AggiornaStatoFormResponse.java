package it.ids.hackathown.api.dto.response;

import java.util.List;

public record AggiornaStatoFormResponse(String statoCorrente, List<String> transizioniPossibili) {
}
