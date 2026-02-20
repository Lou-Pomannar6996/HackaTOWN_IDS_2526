package it.ids.hackathown.domain.state;

import it.ids.hackathown.domain.enums.StatoHackathon;
import org.springframework.stereotype.Component;

@Component
public class CompletedState implements HackathonState {

    @Override
    public StatoHackathon type() {
        return StatoHackathon.CONCLUSO;
    }
}
