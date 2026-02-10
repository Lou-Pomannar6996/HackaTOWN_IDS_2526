package it.ids.hackathown.domain.state;

import it.ids.hackathown.domain.entity.HackathonEntity;

public class HackathonContext {

    private final HackathonEntity hackathon;
    private final HackathonStateFactory stateFactory;

    public HackathonContext(HackathonEntity hackathon, HackathonStateFactory stateFactory) {
        this.hackathon = hackathon;
        this.stateFactory = stateFactory;
    }

    public void registerTeam() {
        currentState().registerTeam(hackathon);
    }

    public void submit() {
        currentState().submit(hackathon);
    }

    public void updateSubmission() {
        currentState().updateSubmission(hackathon);
    }

    public void judgeSubmission() {
        currentState().judgeSubmission(hackathon);
    }

    public void declareWinner() {
        currentState().declareWinner(hackathon);
    }

    public void addMentor() {
        currentState().addMentor(hackathon);
    }

    public void requestSupport() {
        currentState().requestSupport(hackathon);
    }

    public void proposeCall() {
        currentState().proposeCall(hackathon);
    }

    public void startHackathon() {
        currentState().startHackathon(hackathon);
    }

    public void startEvaluation() {
        currentState().startEvaluation(hackathon);
    }

    public void markCompleted() {
        currentState().markCompleted(hackathon);
    }

    private HackathonState currentState() {
        return stateFactory.getState(hackathon.getStateEnum());
    }
}
