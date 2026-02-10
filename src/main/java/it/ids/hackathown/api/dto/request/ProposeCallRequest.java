package it.ids.hackathown.api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public record ProposeCallRequest(@NotEmpty List<@NotNull LocalDateTime> proposedSlots) {
}
