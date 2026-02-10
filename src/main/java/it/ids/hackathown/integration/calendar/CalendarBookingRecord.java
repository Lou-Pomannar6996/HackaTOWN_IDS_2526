package it.ids.hackathown.integration.calendar;

import java.time.LocalDateTime;
import java.util.List;

public record CalendarBookingRecord(
    String bookingId,
    Long hackathonId,
    Long teamId,
    Long mentorId,
    List<LocalDateTime> proposedSlots,
    LocalDateTime createdAt
) {
}
