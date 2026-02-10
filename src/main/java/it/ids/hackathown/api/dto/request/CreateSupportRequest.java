package it.ids.hackathown.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateSupportRequest(@NotBlank String message) {
}
