package it.ids.hackathown.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddMentorRequest(@NotNull @Min(1) Integer mentorUserId) {
}
