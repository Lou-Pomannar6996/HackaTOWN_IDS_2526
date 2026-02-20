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
            hackathon.getId() == null ? null : hackathon.getId().longValue(),
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
            team.getId() == null ? null : team.getId().longValue(),
            team.getNome(),
            team.getMaxMembri()
        );
    }

    public UserResponse toResponse(Utente user) {
        String nomeCompleto = user.getNome() + " " + user.getCognome();
        return new UserResponse(
            user.getId() == null ? null : user.getId().longValue(),
            user.getEmail(),
            nomeCompleto.trim(),
            user.getRoles()
        );
    }

    public InviteResponse toResponse(Invito invite) {
        String email = invite.getDestinatario() == null ? null : invite.getDestinatario().getEmail();
        return new InviteResponse(
            invite.getId() == null ? null : invite.getId().longValue(),
            invite.getTeam() == null ? null : invite.getTeam().getId().longValue(),
            email,
            invite.getStato(),
            invite.getDataInvio()
        );
    }

    public RegistrationResponse toResponse(Iscrizione registration) {
        return new RegistrationResponse(
            registration.getId(),
            registration.getHackathon() == null ? null : registration.getHackathon().getId().longValue(),
            registration.getTeam() == null ? null : registration.getTeam().getId().longValue(),
            registration.getDataIscrizione()
        );
    }

    public SubmissionResponse toResponse(Sottomissione submission) {
        return new SubmissionResponse(
            submission.getId() == null ? null : submission.getId().longValue(),
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
            supportRequest.getId() == null ? null : supportRequest.getId().longValue(),
            supportRequest.getHackathon() == null ? null : supportRequest.getHackathon().getId().longValue(),
            supportRequest.getTeam() == null ? null : supportRequest.getTeam().getId().longValue(),
            supportRequest.getDescrizione(),
            supportRequest.getDataRichiesta(),
            supportRequest.getStato()
        );
    }

    public CallProposalResponse toResponse(CallSupporto proposal) {
        return new CallProposalResponse(
            proposal.getId() == null ? null : proposal.getId().longValue(),
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
            evaluation.getHackathon() == null ? null : evaluation.getHackathon().getId().longValue(),
            evaluation.getSubmission() == null ? null : evaluation.getSubmission().getId().longValue(),
            evaluation.getJudge() == null ? null : evaluation.getJudge().getId().longValue(),
            evaluation.getPunteggio(),
            evaluation.getGiudizio(),
            evaluation.getDataValutazione()
        );
    }

    public ViolationResponse toResponse(SegnalaViolazione violation) {
        return new ViolationResponse(
            violation.getId() == null ? null : violation.getId().longValue(),
            violation.getHackathon() == null ? null : violation.getHackathon().getId().longValue(),
            violation.getMentore() == null ? null : violation.getMentore().getId().longValue(),
            violation.getMotivazione(),
            violation.getDataSegnalazione(),
            violation.getStato()
        );
    }

    public WinnerResponse toResponse(EsitoHackathon winner) {
        return new WinnerResponse(
            winner.getId(),
            winner.getTeam() == null ? null : winner.getTeam().getId().longValue(),
            winner.getDataProclamazione(),
            winner.getNote()
        );
    }
}
