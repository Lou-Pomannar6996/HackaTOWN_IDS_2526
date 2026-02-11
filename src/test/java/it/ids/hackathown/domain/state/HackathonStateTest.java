package it.ids.hackathown.domain.state;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import it.ids.hackathown.domain.entity.HackathonEntity;
import it.ids.hackathown.domain.enums.HackathonStateType;
import it.ids.hackathown.domain.exception.ForbiddenActionForState;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HackathonStateTest {

    private HackathonStateFactory stateFactory;

    @BeforeEach
    void setUp() {
        stateFactory = new HackathonStateFactory(List.of(
            new RegistrationState(),
            new RunningState(),
            new EvaluationState(),
            new CompletedState()
        ));
    }

    @Test
    void registrationState_allowsRegistrationAndTransitionToRunning() {
        HackathonEntity hackathon = new HackathonEntity();
        hackathon.setStateEnum(HackathonStateType.ISCRIZIONI);
        HackathonContext context = new HackathonContext(hackathon, stateFactory);

        assertDoesNotThrow(context::registerTeam);
        context.startHackathon();

        assertEquals(HackathonStateType.IN_CORSO, hackathon.getStateEnum());
    }

    @Test
    void registrationState_blocksSubmission() {
        HackathonEntity hackathon = new HackathonEntity();
        hackathon.setStateEnum(HackathonStateType.ISCRIZIONI);
        HackathonContext context = new HackathonContext(hackathon, stateFactory);

        assertThrows(ForbiddenActionForState.class, context::submit);
    }

    @Test
    void runningState_allowsSubmissionAndTransitionToEvaluation() {
        HackathonEntity hackathon = new HackathonEntity();
        hackathon.setStateEnum(HackathonStateType.IN_CORSO);
        HackathonContext context = new HackathonContext(hackathon, stateFactory);

        assertDoesNotThrow(context::submit);
        assertDoesNotThrow(context::updateSubmission);
        assertDoesNotThrow(context::requestSupport);
        assertDoesNotThrow(context::proposeCall);

        context.startEvaluation();

        assertEquals(HackathonStateType.IN_VALUTAZIONE, hackathon.getStateEnum());
    }

    @Test
    void evaluationState_allowsJudgingAndCompletion() {
        HackathonEntity hackathon = new HackathonEntity();
        hackathon.setStateEnum(HackathonStateType.IN_VALUTAZIONE);
        HackathonContext context = new HackathonContext(hackathon, stateFactory);

        assertDoesNotThrow(context::judgeSubmission);
        assertDoesNotThrow(context::declareWinner);

        context.markCompleted();

        assertEquals(HackathonStateType.CONCLUSO, hackathon.getStateEnum());
    }

    @Test
    void completedState_blocksMutatingActions() {
        HackathonEntity hackathon = new HackathonEntity();
        hackathon.setStateEnum(HackathonStateType.CONCLUSO);
        HackathonContext context = new HackathonContext(hackathon, stateFactory);

        assertThrows(ForbiddenActionForState.class, context::registerTeam);
        assertThrows(ForbiddenActionForState.class, context::submit);
        assertThrows(ForbiddenActionForState.class, context::declareWinner);
    }
}
