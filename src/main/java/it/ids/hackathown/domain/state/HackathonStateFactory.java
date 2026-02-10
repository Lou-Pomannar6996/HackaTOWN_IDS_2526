package it.ids.hackathown.domain.state;

import it.ids.hackathown.domain.enums.HackathonStateType;
import it.ids.hackathown.domain.exception.DomainValidationException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class HackathonStateFactory {

    private final Map<HackathonStateType, HackathonState> stateMap;

    public HackathonStateFactory(List<HackathonState> states) {
        this.stateMap = new EnumMap<>(HackathonStateType.class);
        states.forEach(state -> this.stateMap.put(state.type(), state));
    }

    public HackathonState getState(HackathonStateType stateType) {
        HackathonState state = stateMap.get(stateType);
        if (state == null) {
            throw new DomainValidationException("No state implementation configured for " + stateType);
        }
        return state;
    }
}
