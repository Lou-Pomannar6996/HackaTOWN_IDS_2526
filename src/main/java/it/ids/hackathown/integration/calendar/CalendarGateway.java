package it.ids.hackathown.integration.calendar;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CalendarGateway {

    String bookCall(Long hackathonId, Long teamId, Long mentorId, List<LocalDateTime> proposedSlots);

    Optional<CalendarBookingRecord> findBooking(String bookingId);
}
