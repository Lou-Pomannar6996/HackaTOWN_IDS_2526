package it.ids.hackathown.integration.calendar;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryCalendarGateway implements CalendarGateway {

    private final Map<String, CalendarBookingRecord> bookings = new ConcurrentHashMap<>();

    @Override
    public String bookCall(Long hackathonId, Long teamId, Long mentorId, List<LocalDateTime> proposedSlots) {
        String bookingId = "cal-" + UUID.randomUUID();
        CalendarBookingRecord record = new CalendarBookingRecord(
            bookingId,
            hackathonId,
            teamId,
            mentorId,
            List.copyOf(proposedSlots),
            LocalDateTime.now()
        );
        bookings.put(bookingId, record);
        return bookingId;
    }

    @Override
    public Optional<CalendarBookingRecord> findBooking(String bookingId) {
        return Optional.ofNullable(bookings.get(bookingId));
    }
}
