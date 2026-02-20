package it.ids.hackathown.service.dto;

import java.util.List;

public record AggiornaStatoFormDTO(String statoCorrente, List<String> transizioniPossibili) {
}
