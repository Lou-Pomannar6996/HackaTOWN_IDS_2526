package it.ids.hackathown.domain.state;

import it.ids.hackathown.domain.entity.Hackathon;
import it.ids.hackathown.domain.enums.StatoHackathon;
import it.ids.hackathown.domain.exception.ForbiddenActionForState;

public interface HackathonState {

    StatoHackathon type();

    default void registerTeam(Hackathon hackathon) {
        deny("registerTeam");
    }

    default void submit(Hackathon hackathon) {
        deny("submit");
    }

    default void updateSubmission(Hackathon hackathon) {
        deny("updateSubmission");
    }

    default void judgeSubmission(Hackathon hackathon) {
        deny("judgeSubmission");
    }

    default void declareWinner(Hackathon hackathon) {
        deny("declareWinner");
    }

    default void addMentor(Hackathon hackathon) {
        deny("addMentor");
    }

    default void requestSupport(Hackathon hackathon) {
        deny("requestSupport");
    }

    default void proposeCall(Hackathon hackathon) {
        deny("proposeCall");
    }

    default void startHackathon(Hackathon hackathon) {
        deny("startHackathon");
    }

    default void startEvaluation(Hackathon hackathon) {
        deny("startEvaluation");
    }

    default void markCompleted(Hackathon hackathon) {
        deny("markCompleted");
    }

    private void deny(String action) {
        throw new ForbiddenActionForState(
            "Action '" + action + "' is not allowed while hackathon is in state " + type()
        );
    }
}
