package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.HackathonEntity;
import it.ids.hackathown.domain.entity.SubmissionEntity;
import it.ids.hackathown.domain.entity.SupportRequestEntity;
import it.ids.hackathown.domain.entity.TeamEntity;
import it.ids.hackathown.domain.entity.TeamInviteEntity;
import it.ids.hackathown.domain.entity.UserEntity;
import it.ids.hackathown.domain.enums.UserRole;
import it.ids.hackathown.domain.exception.DomainException;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.domain.exception.ForbiddenActionForState;
import it.ids.hackathown.domain.exception.NotFoundException;
import it.ids.hackathown.repository.HackathonRepository;
import it.ids.hackathown.repository.SubmissionRepository;
import it.ids.hackathown.repository.SupportRequestRepository;
import it.ids.hackathown.repository.TeamInviteRepository;
import it.ids.hackathown.repository.TeamRepository;
import it.ids.hackathown.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccessControlService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamInviteRepository teamInviteRepository;
    private final HackathonRepository hackathonRepository;
    private final SubmissionRepository submissionRepository;
    private final SupportRequestRepository supportRequestRepository;

    public UserEntity requireUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    public TeamEntity requireTeam(Long teamId) {
        return teamRepository.findById(teamId)
            .orElseThrow(() -> new NotFoundException("Team not found: " + teamId));
    }

    public TeamInviteEntity requireInvite(Long inviteId) {
        return teamInviteRepository.findById(inviteId)
            .orElseThrow(() -> new NotFoundException("Invite not found: " + inviteId));
    }

    public HackathonEntity requireHackathon(Long hackathonId) {
        return hackathonRepository.findById(hackathonId)
            .orElseThrow(() -> new NotFoundException("Hackathon not found: " + hackathonId));
    }

    public SubmissionEntity requireSubmission(Long submissionId) {
        return submissionRepository.findById(submissionId)
            .orElseThrow(() -> new NotFoundException("Submission not found: " + submissionId));
    }

    public SupportRequestEntity requireSupportRequest(Long supportRequestId) {
        return supportRequestRepository.findById(supportRequestId)
            .orElseThrow(() -> new NotFoundException("Support request not found: " + supportRequestId));
    }

    public TeamEntity requireTeamOfUser(Long userId) {
        return teamRepository.findByMembers_Id(userId)
            .orElseThrow(() -> new DomainValidationException("User " + userId + " does not belong to any team"));
    }

    public void assertTeamMember(TeamEntity team, Long userId) {
        boolean isMember = team.getMembers().stream().anyMatch(member -> member.getId().equals(userId));
        if (!isMember) {
            throw new DomainException(HttpStatus.FORBIDDEN, "User is not a member of team " + team.getId());
        }
    }

    public void assertOrganizer(HackathonEntity hackathon, Long userId) {
        if (!hackathon.getOrganizer().getId().equals(userId)) {
            throw new DomainException(HttpStatus.FORBIDDEN, "Only the organizer can perform this action");
        }
    }

    public void assertJudge(HackathonEntity hackathon, Long userId) {
        if (!hackathon.getJudge().getId().equals(userId)) {
            throw new DomainException(HttpStatus.FORBIDDEN, "Only the assigned judge can perform this action");
        }
    }

    public void assertMentorAssigned(HackathonEntity hackathon, Long userId) {
        boolean assigned = hackathon.getMentors().stream().anyMatch(mentor -> mentor.getId().equals(userId));
        if (!assigned) {
            throw new DomainException(HttpStatus.FORBIDDEN, "Mentor is not assigned to this hackathon");
        }
    }

    public void assertOrganizerOrJudge(HackathonEntity hackathon, Long userId) {
        boolean allowed = hackathon.getOrganizer().getId().equals(userId) || hackathon.getJudge().getId().equals(userId);
        if (!allowed) {
            throw new DomainException(HttpStatus.FORBIDDEN, "Action allowed only to organizer or assigned judge");
        }
    }

    public void assertUserRole(UserEntity user, UserRole role) {
        if (!user.getRoles().contains(role)) {
            throw new DomainException(HttpStatus.FORBIDDEN, "User " + user.getId() + " must have role " + role);
        }
    }
}
