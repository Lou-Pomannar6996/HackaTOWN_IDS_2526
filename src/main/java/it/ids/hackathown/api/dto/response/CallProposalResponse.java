package it.ids.hackathown.api.dto.response;

import it.ids.hackathown.domain.enums.CallProposalStatus;
import java.time.LocalDateTime;

public record CallProposalResponse(
    Long id,
    Long hackathonId,
    Long teamId,
    Long mentorId,
    String proposedSlots,
    String calendarBookingId,
    CallProposalStatus status,
    LocalDateTime createdAt
) {
}
