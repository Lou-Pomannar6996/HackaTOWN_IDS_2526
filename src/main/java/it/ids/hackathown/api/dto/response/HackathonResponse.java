package it.ids.hackathown.api.dto.response;

import it.ids.hackathown.domain.enums.StatoHackathon;
import java.math.BigDecimal;
import java.util.Date;

public record HackathonResponse(
    Long id,
    String name,
    String description,
    String rules,
    Date registrationDeadline,
    Date startDate,
    Date endDate,
    String location,
    BigDecimal prizeMoney,
    Integer maxTeamSize,
    StatoHackathon state
) {
}
