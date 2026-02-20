package it.ids.hackathown.domain.state;

import it.ids.hackathown.domain.entity.Hackathon;
import it.ids.hackathown.domain.enums.StatoHackathon;
import org.springframework.stereotype.Component;

@Component
public class RunningState implements HackathonState {

    @Override
    public StatoHackathon type() {
        return StatoHackathon.IN_CORSO;
    }

    @Override
    public void submit(Hackathon hackathon) {
        // allowed
    }

    @Override
    public void updateSubmission(Hackathon hackathon) {
        // allowed
    }

    @Override
    public void requestSupport(Hackathon hackathon) {
        // allowed
    }

    @Override
    public void proposeCall(Hackathon hackathon) {
        // allowed
    }

    @Override
    public void startEvaluation(Hackathon hackathon) {
        hackathon.setStato(StatoHackathon.IN_VALUTAZIONE);
    }
}
