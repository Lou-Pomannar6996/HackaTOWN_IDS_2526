package it.ids.hackathown.domain.state;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import it.ids.hackathown.domain.entity.Hackathon;
import it.ids.hackathown.domain.enums.StatoHackathon;
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
        Hackathon hackathon = new Hackathon();
        hackathon.setStato(StatoHackathon.ISCRIZIONI);
        HackathonContext context = new HackathonContext(hackathon, stateFactory);

        assertDoesNotThrow(context::registerTeam);
        context.startHackathon();

        assertEquals(StatoHackathon.IN_CORSO, hackathon.getStato());
    }

    @Test
    void registrationState_blocksSubmission() {
        Hackathon hackathon = new Hackathon();
        hackathon.setStato(StatoHackathon.ISCRIZIONI);
        HackathonContext context = new HackathonContext(hackathon, stateFactory);

        assertThrows(ForbiddenActionForState.class, context::submit);
    }

    @Test
    void runningState_allowsSubmissionAndTransitionToEvaluation() {
        Hackathon hackathon = new Hackathon();
        hackathon.setStato(StatoHackathon.IN_CORSO);
        HackathonContext context = new HackathonContext(hackathon, stateFactory);

        assertDoesNotThrow(context::submit);
        assertDoesNotThrow(context::updateSubmission);
        assertDoesNotThrow(context::requestSupport);
        assertDoesNotThrow(context::proposeCall);

        context.startEvaluation();

        assertEquals(StatoHackathon.IN_VALUTAZIONE, hackathon.getStato());
    }

    @Test
    void evaluationState_allowsJudgingAndCompletion() {
        Hackathon hackathon = new Hackathon();
        hackathon.setStato(StatoHackathon.IN_VALUTAZIONE);
        HackathonContext context = new HackathonContext(hackathon, stateFactory);

        assertDoesNotThrow(context::judgeSubmission);
        assertDoesNotThrow(context::declareWinner);

        context.markCompleted();

        assertEquals(StatoHackathon.CONCLUSO, hackathon.getStato());
    }

    @Test
    void completedState_blocksMutatingActions() {
        Hackathon hackathon = new Hackathon();
        hackathon.setStato(StatoHackathon.CONCLUSO);
        HackathonContext context = new HackathonContext(hackathon, stateFactory);

        assertThrows(ForbiddenActionForState.class, context::registerTeam);
        assertThrows(ForbiddenActionForState.class, context::submit);
        assertThrows(ForbiddenActionForState.class, context::declareWinner);
    }
}
