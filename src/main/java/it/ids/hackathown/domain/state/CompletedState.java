package it.ids.hackathown.domain.state;

import it.ids.hackathown.domain.enums.HackathonStateType;
import org.springframework.stereotype.Component;

@Component
public class CompletedState implements HackathonState {

    @Override
    public HackathonStateType type() {
        return HackathonStateType.CONCLUSO;
    }
}
