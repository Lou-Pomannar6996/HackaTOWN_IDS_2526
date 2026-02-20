package it.ids.hackathown.domain.state;

import it.ids.hackathown.domain.entity.Hackathon;
import it.ids.hackathown.domain.enums.StatoHackathon;
import org.springframework.stereotype.Component;

@Component
public class EvaluationState implements HackathonState {

    @Override
    public StatoHackathon type() {
        return StatoHackathon.IN_VALUTAZIONE;
    }

    @Override
    public void judgeSubmission(Hackathon hackathon) {
        // allowed
    }

    @Override
    public void declareWinner(Hackathon hackathon) {
        // allowed
    }

    @Override
    public void markCompleted(Hackathon hackathon) {
        hackathon.setStato(StatoHackathon.CONCLUSO);
    }
}
