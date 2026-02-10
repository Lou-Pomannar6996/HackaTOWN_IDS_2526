package it.ids.hackathown.domain.state;

import it.ids.hackathown.domain.entity.HackathonEntity;
import it.ids.hackathown.domain.enums.HackathonStateType;
import it.ids.hackathown.domain.exception.ForbiddenActionForState;

public interface HackathonState {

    HackathonStateType type();

    default void registerTeam(HackathonEntity hackathon) {
        deny("registerTeam");
    }

    default void submit(HackathonEntity hackathon) {
        deny("submit");
    }

    default void updateSubmission(HackathonEntity hackathon) {
        deny("updateSubmission");
    }

    default void judgeSubmission(HackathonEntity hackathon) {
        deny("judgeSubmission");
    }

    default void declareWinner(HackathonEntity hackathon) {
        deny("declareWinner");
    }

    default void addMentor(HackathonEntity hackathon) {
        deny("addMentor");
    }

    default void requestSupport(HackathonEntity hackathon) {
        deny("requestSupport");
    }

    default void proposeCall(HackathonEntity hackathon) {
        deny("proposeCall");
    }

    default void startHackathon(HackathonEntity hackathon) {
        deny("startHackathon");
    }

    default void startEvaluation(HackathonEntity hackathon) {
        deny("startEvaluation");
    }

    default void markCompleted(HackathonEntity hackathon) {
        deny("markCompleted");
    }

    private void deny(String action) {
        throw new ForbiddenActionForState(
            "Action '" + action + "' is not allowed while hackathon is in state " + type()
        );
    }
}
