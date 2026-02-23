package it.ids.hackathown.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PianificaCallRequest(
    @NotEmpty List<@NotBlank String> slotPreferiti
) {
}
