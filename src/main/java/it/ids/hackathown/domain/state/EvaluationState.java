package it.ids.hackathown.domain.state;

import it.ids.hackathown.domain.entity.Hackathon;
import it.ids.hackathown.domain.enums.HackathonStateType;
import org.springframework.stereotype.Component;

@Component
public class EvaluationState implements HackathonState {

    @Override
    public HackathonStateType type() {
        return HackathonStateType.IN_VALUTAZIONE;
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
        hackathon.setStato(HackathonStateType.CONCLUSO);
    }
}
