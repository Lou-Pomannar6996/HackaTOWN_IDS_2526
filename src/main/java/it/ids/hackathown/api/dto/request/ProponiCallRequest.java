package it.ids.hackathown.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;

public record ProponiCallRequest(
    @NotNull Integer mentorId,
    @NotNull Integer richiestaId,
    @NotNull Date dataProposta,
    @NotNull @Min(1) Integer durataMin,
    @NotBlank String calendarEventId
) {
}
