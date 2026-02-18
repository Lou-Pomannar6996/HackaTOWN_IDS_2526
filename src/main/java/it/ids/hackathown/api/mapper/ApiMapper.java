package it.ids.hackathown.api.mapper;

import it.ids.hackathown.api.dto.response.CallProposalResponse;
import it.ids.hackathown.api.dto.response.EvaluationResponse;
import it.ids.hackathown.api.dto.response.HackathonResponse;
import it.ids.hackathown.api.dto.response.InviteResponse;
import it.ids.hackathown.api.dto.response.RegistrationResponse;
import it.ids.hackathown.api.dto.response.SubmissionResponse;
import it.ids.hackathown.api.dto.response.SupportRequestResponse;
import it.ids.hackathown.api.dto.response.TeamResponse;
import it.ids.hackathown.api.dto.response.UserResponse;
import it.ids.hackathown.api.dto.response.ViolationResponse;
import it.ids.hackathown.api.dto.response.WinnerResponse;
import it.ids.hackathown.domain.entity.CallSupporto;
import it.ids.hackathown.domain.entity.Valutazione;
import it.ids.hackathown.domain.entity.Hackathon;
import it.ids.hackathown.domain.entity.Iscrizione;
import it.ids.hackathown.domain.entity.Sottomissione;
import it.ids.hackathown.domain.entity.RichiestaSupporto;
import it.ids.hackathown.domain.entity.Team;
import it.ids.hackathown.domain.entity.Invito;
import it.ids.hackathown.domain.entity.Utente;
import it.ids.hackathown.domain.entity.SegnalazioneViolazione;
import it.ids.hackathown.domain.entity.EsitoHackathon;
import org.springframework.stereotype.Component;

@Component
public class ApiMapper {

    public HackathonResponse toResponse(Hackathon hackathon) {
        return new HackathonResponse(
            hackathon.getId(),
            hackathon.getNome(),
            hackathon.getRegolamento(),
            hackathon.getScadenzaIscrizioni(),
            hackathon.getDataInizio(),
            hackathon.getDataFine(),
            hackathon.getLuogo(),
            hackathon.getPremio(),
            hackathon.getMaxTeamSize(),
            hackathon.getStato(),
            hackathon.getScoringPolicyType(),
            hackathon.getValidationPolicyType(),
            hackathon.getOrganizer().getId(),
            hackathon.getJudge().getId()
        );
    }

    public TeamResponse toResponse(Team team) {
        return new TeamResponse(team.getId(), team.getNome(), team.getMaxMembri());
    }

    public UserResponse toResponse(Utente user) {
        String nomeCompleto = user.getNome() + " " + user.getCognome();
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            nomeCompleto.trim(),
            user.getRoles()
        );
    }

    public InviteResponse toResponse(Invito invite) {
        String email = invite.getDestinatario() == null ? null : invite.getDestinatario().getEmail();
        return new InviteResponse(
            invite.getId(),
            invite.getTeam().getId(),
            email,
            invite.getStato(),
            invite.getDataInvio()
        );
    }

    public RegistrationResponse toResponse(Iscrizione registration) {
        return new RegistrationResponse(
            registration.getId(),
            registration.getHackathon().getId(),
            registration.getTeam().getId(),
            registration.getDataIscrizione()
        );
    }

    public SubmissionResponse toResponse(Sottomissione submission) {
        return new SubmissionResponse(
            submission.getId(),
            submission.getHackathon().getId(),
            submission.getTeam().getId(),
            submission.getUrlRepo(),
            submission.getFileRef(),
            submission.getDescrizione(),
            submission.getDataUltimoAggiornamento(),
            submission.getStatus()
        );
    }

    public SupportRequestResponse toResponse(RichiestaSupporto supportRequest) {
        return new SupportRequestResponse(
            supportRequest.getId(),
            supportRequest.getHackathon().getId(),
            supportRequest.getTeam().getId(),
            supportRequest.getDescrizione(),
            supportRequest.getDataRichiesta(),
            supportRequest.getStato()
        );
    }

    public CallProposalResponse toResponse(CallSupporto proposal) {
        String slot = proposal.getDataInizio() == null ? null
            : proposal.getDataInizio() + " (" + proposal.getDurataMin() + " min)";
        return new CallProposalResponse(
            proposal.getId(),
            proposal.getHackathon().getId(),
            proposal.getTeam().getId(),
            proposal.getMentor().getId(),
            slot,
            proposal.getCalendarEventId(),
            proposal.getStato(),
            proposal.getDataProposta()
        );
    }

    public EvaluationResponse toResponse(Valutazione evaluation) {
        return new EvaluationResponse(
            evaluation.getId(),
            evaluation.getHackathon().getId(),
            evaluation.getSubmission().getId(),
            evaluation.getJudge().getId(),
            evaluation.getPunteggio(),
            evaluation.getGiudizio(),
            evaluation.getDataValutazione()
        );
    }

    public ViolationResponse toResponse(SegnalazioneViolazione violation) {
        return new ViolationResponse(
            violation.getId(),
            violation.getHackathon().getId(),
            violation.getTeam().getId(),
            violation.getMentor().getId(),
            violation.getMotivaizone(),
            violation.getDataSegnalazione()
        );
    }

    public WinnerResponse toResponse(EsitoHackathon winner) {
        return new WinnerResponse(
            winner.getId(),
            winner.getTeam().getId(),
            winner.getDataProclamazione(),
            winner.getNote()
        );
    }
}
