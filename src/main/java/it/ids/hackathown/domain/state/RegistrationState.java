package it.ids.hackathown.domain.state;

import it.ids.hackathown.domain.entity.HackathonEntity;
import it.ids.hackathown.domain.enums.HackathonStateType;
import org.springframework.stereotype.Component;

@Component
public class RegistrationState implements HackathonState {

    @Override
    public HackathonStateType type() {
        return HackathonStateType.ISCRIZIONI;
    }

    @Override
    public void registerTeam(HackathonEntity hackathon) {
        // allowed
    }

    @Override
    public void addMentor(HackathonEntity hackathon) {
        // allowed
    }

    @Override
    public void startHackathon(HackathonEntity hackathon) {
        hackathon.setStateEnum(HackathonStateType.IN_CORSO);
    }
}
