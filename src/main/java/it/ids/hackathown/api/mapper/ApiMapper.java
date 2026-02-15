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
import it.ids.hackathown.domain.entity.CallProposalEntity;
import it.ids.hackathown.domain.entity.EvaluationEntity;
import it.ids.hackathown.domain.entity.HackathonEntity;
import it.ids.hackathown.domain.entity.RegistrationEntity;
import it.ids.hackathown.domain.entity.SubmissionEntity;
import it.ids.hackathown.domain.entity.SupportRequestEntity;
import it.ids.hackathown.domain.entity.TeamEntity;
import it.ids.hackathown.domain.entity.TeamInviteEntity;
import it.ids.hackathown.domain.entity.UserEntity;
import it.ids.hackathown.domain.entity.ViolationReportEntity;
import it.ids.hackathown.domain.entity.WinnerEntity;
import org.springframework.stereotype.Component;

@Component
public class ApiMapper {

    public HackathonResponse toResponse(HackathonEntity hackathon) {
        return new HackathonResponse(
            hackathon.getId(),
            hackathon.getName(),
            hackathon.getRules(),
            hackathon.getRegistrationDeadline(),
            hackathon.getStartDate(),
            hackathon.getEndDate(),
            hackathon.getLocation(),
            hackathon.getPrizeMoney(),
            hackathon.getMaxTeamSize(),
            hackathon.getStateEnum(),
            hackathon.getScoringPolicyType(),
            hackathon.getValidationPolicyType(),
            hackathon.getOrganizer().getId(),
            hackathon.getJudge().getId()
        );
    }

    public TeamResponse toResponse(TeamEntity team) {
        return new TeamResponse(team.getId(), team.getName(), team.getMaxSize());
    }

    public UserResponse toResponse(UserEntity user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getRoles()
        );
    }

    public InviteResponse toResponse(TeamInviteEntity invite) {
        return new InviteResponse(
            invite.getId(),
            invite.getTeam().getId(),
            invite.getInvitedEmail(),
            invite.getStatus(),
            invite.getCreatedAt()
        );
    }

    public RegistrationResponse toResponse(RegistrationEntity registration) {
        return new RegistrationResponse(
            registration.getId(),
            registration.getHackathon().getId(),
            registration.getTeam().getId(),
            registration.getCreatedAt()
        );
    }

    public SubmissionResponse toResponse(SubmissionEntity submission) {
        return new SubmissionResponse(
            submission.getId(),
            submission.getHackathon().getId(),
            submission.getTeam().getId(),
            submission.getRepoUrl(),
            submission.getFileRef(),
            submission.getDescription(),
            submission.getUpdatedAt(),
            submission.getStatus()
        );
    }

    public SupportRequestResponse toResponse(SupportRequestEntity supportRequest) {
        return new SupportRequestResponse(
            supportRequest.getId(),
            supportRequest.getHackathon().getId(),
            supportRequest.getTeam().getId(),
            supportRequest.getMessage(),
            supportRequest.getCreatedAt(),
            supportRequest.getStatus()
        );
    }

    public CallProposalResponse toResponse(CallProposalEntity proposal) {
        return new CallProposalResponse(
            proposal.getId(),
            proposal.getHackathon().getId(),
            proposal.getTeam().getId(),
            proposal.getMentor().getId(),
            proposal.getProposedSlots(),
            proposal.getCalendarBookingId(),
            proposal.getStatus(),
            proposal.getCreatedAt()
        );
    }

    public EvaluationResponse toResponse(EvaluationEntity evaluation) {
        return new EvaluationResponse(
            evaluation.getId(),
            evaluation.getHackathon().getId(),
            evaluation.getSubmission().getId(),
            evaluation.getJudge().getId(),
            evaluation.getScore0to10(),
            evaluation.getComment(),
            evaluation.getCreatedAt()
        );
    }

    public ViolationResponse toResponse(ViolationReportEntity violation) {
        return new ViolationResponse(
            violation.getId(),
            violation.getHackathon().getId(),
            violation.getTeam().getId(),
            violation.getMentor().getId(),
            violation.getReason(),
            violation.getCreatedAt()
        );
    }

    public WinnerResponse toResponse(WinnerEntity winner) {
        return new WinnerResponse(
            winner.getHackathonId(),
            winner.getTeam().getId(),
            winner.getDeclaredAt(),
            winner.getPaymentTxId()
        );
    }
}
