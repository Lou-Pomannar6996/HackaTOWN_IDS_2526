package it.ids.hackathown.domain.state;

import it.ids.hackathown.domain.enums.StatoHackathon;
import it.ids.hackathown.domain.exception.DomainValidationException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class HackathonStateFactory {

    private final Map<StatoHackathon, HackathonState> stateMap;

    public HackathonStateFactory(List<HackathonState> states) {
        this.stateMap = new EnumMap<>(StatoHackathon.class);
        states.forEach(state -> this.stateMap.put(state.type(), state));
    }

    public HackathonState getState(StatoHackathon stato) {
        HackathonState state = stateMap.get(stato);
        if (state == null) {
            throw new DomainValidationException("No state implementation configured for " + stato);
        }
        return state;
    }
}
