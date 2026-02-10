package it.ids.hackathown.domain.state;

import it.ids.hackathown.domain.entity.HackathonEntity;
import it.ids.hackathown.domain.enums.HackathonStateType;
import org.springframework.stereotype.Component;

@Component
public class RunningState implements HackathonState {

    @Override
    public HackathonStateType type() {
        return HackathonStateType.IN_CORSO;
    }

    @Override
    public void submit(HackathonEntity hackathon) {
        // allowed
    }

    @Override
    public void updateSubmission(HackathonEntity hackathon) {
        // allowed
    }

    @Override
    public void requestSupport(HackathonEntity hackathon) {
        // allowed
    }

    @Override
    public void proposeCall(HackathonEntity hackathon) {
        // allowed
    }

    @Override
    public void startEvaluation(HackathonEntity hackathon) {
        hackathon.setStateEnum(HackathonStateType.IN_VALUTAZIONE);
    }
}
