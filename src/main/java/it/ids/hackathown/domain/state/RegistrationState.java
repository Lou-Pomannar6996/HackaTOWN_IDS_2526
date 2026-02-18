package it.ids.hackathown.domain.state;

import it.ids.hackathown.domain.entity.Hackathon;
import it.ids.hackathown.domain.enums.HackathonStateType;
import org.springframework.stereotype.Component;

@Component
public class RegistrationState implements HackathonState {

    @Override
    public HackathonStateType type() {
        return HackathonStateType.ISCRIZIONI;
    }

    @Override
    public void registerTeam(Hackathon hackathon) {
        // allowed
    }

    @Override
    public void addMentor(Hackathon hackathon) {
        // allowed
    }

    @Override
    public void startHackathon(Hackathon hackathon) {
        hackathon.setStato(HackathonStateType.IN_CORSO);
    }
}
