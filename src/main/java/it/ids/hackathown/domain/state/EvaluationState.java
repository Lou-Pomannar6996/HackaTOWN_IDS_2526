package it.ids.hackathown.domain.state;

import it.ids.hackathown.domain.entity.HackathonEntity;
import it.ids.hackathown.domain.enums.HackathonStateType;
import org.springframework.stereotype.Component;

@Component
public class EvaluationState implements HackathonState {

    @Override
    public HackathonStateType type() {
        return HackathonStateType.IN_VALUTAZIONE;
    }

    @Override
    public void judgeSubmission(HackathonEntity hackathon) {
        // allowed
    }

    @Override
    public void declareWinner(HackathonEntity hackathon) {
        // allowed
    }

    @Override
    public void markCompleted(HackathonEntity hackathon) {
        hackathon.setStateEnum(HackathonStateType.CONCLUSO);
    }
}
