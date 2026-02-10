package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.TeamInviteEntity;
import it.ids.hackathown.domain.enums.InviteStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamInviteRepository extends JpaRepository<TeamInviteEntity, Long> {

    Optional<TeamInviteEntity> findByIdAndInvitedEmailIgnoreCase(Long id, String invitedEmail);

    List<TeamInviteEntity> findByTeam_Id(Long teamId);

    boolean existsByTeam_IdAndInvitedEmailIgnoreCaseAndStatus(Long teamId, String email, InviteStatus status);
}
