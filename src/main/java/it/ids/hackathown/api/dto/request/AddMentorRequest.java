package it.ids.hackathown.api.dto.request;

import jakarta.validation.constraints.NotNull;

public record AddMentorRequest(@NotNull Long mentorUserId) {
}
