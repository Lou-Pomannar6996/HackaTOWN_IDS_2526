package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.TeamEntity;
import it.ids.hackathown.domain.entity.TeamInviteEntity;
import it.ids.hackathown.domain.entity.UserEntity;
import it.ids.hackathown.domain.enums.InviteStatus;
import it.ids.hackathown.domain.exception.ConflictException;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.domain.exception.NotFoundException;
import it.ids.hackathown.repository.TeamInviteRepository;
import it.ids.hackathown.repository.TeamRepository;
import it.ids.hackathown.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamInviteRepository teamInviteRepository;
    private final UserRepository userRepository;
    private final AccessControlService accessControlService;

    @Transactional
    public TeamEntity createTeam(Long currentUserId, String name, Integer maxSize) {
        UserEntity creator = accessControlService.requireUser(currentUserId);

        if (teamRepository.existsByMembers_Id(currentUserId)) {
            throw new ConflictException("User already belongs to a team");
        }
        if (teamRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("A team with this name already exists");
        }
        if (maxSize == null || maxSize < 1) {
            throw new DomainValidationException("maxSize must be greater than zero");
        }

        TeamEntity team = TeamEntity.builder()
            .name(name)
            .maxSize(maxSize)
            .build();
        team.getMembers().add(creator);

        TeamEntity saved = teamRepository.save(team);
        log.info("Created team {} by user {}", saved.getId(), currentUserId);
        return saved;
    }

    @Transactional
    public TeamInviteEntity inviteUser(Long teamId, Long currentUserId, String invitedEmail) {
        TeamEntity team = accessControlService.requireTeam(teamId);
        accessControlService.requireUser(currentUserId);
        accessControlService.assertTeamMember(team, currentUserId);

        if (teamInviteRepository.existsByTeam_IdAndInvitedEmailIgnoreCaseAndStatus(teamId, invitedEmail, InviteStatus.PENDING)) {
            throw new ConflictException("A pending invite for this email already exists");
        }

        TeamInviteEntity invite = TeamInviteEntity.builder()
            .team(team)
            .invitedEmail(invitedEmail)
            .invitedUser(userRepository.findByEmailIgnoreCase(invitedEmail).orElse(null))
            .status(InviteStatus.PENDING)
            .build();

        TeamInviteEntity saved = teamInviteRepository.save(invite);
        log.info("Created invite {} for team {}", saved.getId(), teamId);
        return saved;
    }

    @Transactional
    public TeamInviteEntity acceptInvite(Long inviteId, Long currentUserId) {
        UserEntity user = accessControlService.requireUser(currentUserId);

        TeamInviteEntity invite = teamInviteRepository.findByIdAndInvitedEmailIgnoreCase(inviteId, user.getEmail())
            .orElseThrow(() -> new NotFoundException("Invite not found for current user"));

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new ConflictException("Invite is not pending anymore");
        }
        if (teamRepository.existsByMembers_Id(currentUserId)) {
            throw new ConflictException("User already belongs to a team");
        }

        TeamEntity team = invite.getTeam();
        if (team.getMembers().size() >= team.getMaxSize()) {
            throw new ConflictException("Team is already full");
        }

        team.getMembers().add(user);
        invite.setInvitedUser(user);
        invite.setStatus(InviteStatus.ACCEPTED);

        teamRepository.save(team);
        TeamInviteEntity saved = teamInviteRepository.save(invite);
        log.info("User {} accepted invite {}", currentUserId, inviteId);
        return saved;
    }

    @Transactional
    public TeamEntity leaveTeam(Long teamId, Long currentUserId) {
        TeamEntity team = accessControlService.requireTeam(teamId);
        accessControlService.requireUser(currentUserId);
        accessControlService.assertTeamMember(team, currentUserId);

        team.getMembers().removeIf(member -> member.getId().equals(currentUserId));
        TeamEntity saved = teamRepository.save(team);
        log.info("User {} left team {}", currentUserId, teamId);
        return saved;
    }
}
