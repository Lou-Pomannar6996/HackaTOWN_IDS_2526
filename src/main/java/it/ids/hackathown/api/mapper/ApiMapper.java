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
import it.ids.hackathown.domain.entity.SegnalaViolazione;
import it.ids.hackathown.domain.entity.EsitoHackathon;
import org.springframework.stereotype.Component;

@Component
public class ApiMapper {

    public HackathonResponse toResponse(Hackathon hackathon) {
        return new HackathonResponse(
            hackathon.getId(),
            hackathon.getNome(),
            hackathon.getDescrizione(),
            hackathon.getRegolamento(),
            hackathon.getScadenzaIscrizioni(),
            hackathon.getDataInizio(),
            hackathon.getDataFine(),
            hackathon.getLuogo(),
            hackathon.getPremio(),
            hackathon.getMaxTeamSize(),
            hackathon.getStato()
        );
    }

    public TeamResponse toResponse(Team team) {
        return new TeamResponse(
            team.getId(),
            team.getNome(),
            team.getMaxMembri()
        );
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
            invite.getTeam() == null ? null : invite.getTeam().getId(),
            email,
            invite.getStato(),
            invite.getDataInvio()
        );
    }

    public RegistrationResponse toResponse(Iscrizione registration) {
        return new RegistrationResponse(
            registration.getId(),
            registration.getHackathon() == null ? null : registration.getHackathon().getId(),
            registration.getTeam() == null ? null : registration.getTeam().getId(),
            registration.getDataIscrizione()
        );
    }

    public SubmissionResponse toResponse(Sottomissione submission) {
        return new SubmissionResponse(
            submission.getId(),
            submission.getIscrizione() == null ? null : submission.getIscrizione().getId(),
            submission.getTitolo(),
            submission.getDescrizione(),
            submission.getUrlRepo(),
            submission.getDataUltimoAggiornamento(),
            submission.getDataInvio()
        );
    }

    public SupportRequestResponse toResponse(RichiestaSupporto supportRequest) {
        return new SupportRequestResponse(
            supportRequest.getId(),
            supportRequest.getHackathon() == null ? null : supportRequest.getHackathon().getId(),
            supportRequest.getTeam() == null ? null : supportRequest.getTeam().getId(),
            supportRequest.getDescrizione(),
            supportRequest.getDataRichiesta(),
            supportRequest.getStato()
        );
    }

    public CallProposalResponse toResponse(CallSupporto proposal) {
        return new CallProposalResponse(
            proposal.getId(),
            proposal.getDataProposta(),
            proposal.getDataInizio(),
            proposal.getDurataMin(),
            proposal.getCalendarEventId(),
            proposal.getStato()
        );
    }

    public EvaluationResponse toResponse(Valutazione evaluation) {
        return new EvaluationResponse(
            evaluation.getId(),
            evaluation.getHackathon() == null ? null : evaluation.getHackathon().getId(),
            evaluation.getSubmission() == null ? null : evaluation.getSubmission().getId(),
            evaluation.getJudge() == null ? null : evaluation.getJudge().getId(),
            evaluation.getPunteggio(),
            evaluation.getGiudizio(),
            evaluation.getDataValutazione()
        );
    }

    public ViolationResponse toResponse(SegnalaViolazione violation) {
        return new ViolationResponse(
            violation.getId(),
            violation.getHackathon() == null ? null : violation.getHackathon().getId(),
            violation.getMentore() == null ? null : violation.getMentore().getId(),
            violation.getDescrizione(),
            violation.getMotivazione(),
            violation.getDataSegnalazione(),
            violation.getStato()
        );
    }

    public WinnerResponse toResponse(EsitoHackathon winner) {
        return new WinnerResponse(
            winner.getId(),
            winner.getTeam() == null ? null : winner.getTeam().getId(),
            winner.getDataProclamazione(),
            winner.getNote()
        );
    }
}
