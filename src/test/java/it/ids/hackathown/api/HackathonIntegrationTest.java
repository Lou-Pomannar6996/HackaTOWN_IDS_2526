package it.ids.hackathown.api;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.ids.hackathown.domain.entity.CallProposalEntity;
import it.ids.hackathown.domain.entity.EvaluationEntity;
import it.ids.hackathown.domain.entity.HackathonEntity;
import it.ids.hackathown.domain.entity.RegistrationEntity;
import it.ids.hackathown.domain.entity.SubmissionEntity;
import it.ids.hackathown.domain.entity.SupportRequestEntity;
import it.ids.hackathown.domain.entity.TeamEntity;
import it.ids.hackathown.domain.entity.UserEntity;
import it.ids.hackathown.domain.entity.ViolationReportEntity;
import it.ids.hackathown.domain.entity.WinnerEntity;
import it.ids.hackathown.domain.enums.CallProposalStatus;
import it.ids.hackathown.domain.enums.HackathonStateType;
import it.ids.hackathown.domain.enums.ScoringPolicyType;
import it.ids.hackathown.domain.enums.SubmissionStatus;
import it.ids.hackathown.domain.enums.SupportRequestStatus;
import it.ids.hackathown.domain.enums.UserRole;
import it.ids.hackathown.domain.enums.ValidationPolicyType;
import it.ids.hackathown.repository.CallProposalRepository;
import it.ids.hackathown.repository.EvaluationRepository;
import it.ids.hackathown.repository.HackathonRepository;
import it.ids.hackathown.repository.RegistrationRepository;
import it.ids.hackathown.repository.SubmissionRepository;
import it.ids.hackathown.repository.SupportRequestRepository;
import it.ids.hackathown.repository.TeamRepository;
import it.ids.hackathown.repository.UserRepository;
import it.ids.hackathown.repository.ViolationReportRepository;
import it.ids.hackathown.repository.WinnerRepository;
import it.ids.hackathown.service.HackathonService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class HackathonIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private HackathonRepository hackathonRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private EvaluationRepository evaluationRepository;

    @Autowired
    private WinnerRepository winnerRepository;

    @Autowired
    private SupportRequestRepository supportRequestRepository;

    @Autowired
    private CallProposalRepository callProposalRepository;

    @Autowired
    private ViolationReportRepository violationReportRepository;

    @Autowired
    private HackathonService hackathonService;

    @BeforeEach
    void beforeEach() {
        cleanupAll();
    }

    @Test
    void declareWinner_endpointSelectsBestSubmissionAndCompletesHackathon() throws Exception {
        UserEntity organizer = saveUser("organizer@hackhub.dev", "Organizer", Set.of(UserRole.ORGANIZER));
        UserEntity judge = saveUser("judge@hackhub.dev", "Judge", Set.of(UserRole.JUDGE));
        UserEntity memberA = saveUser("member-a@hackhub.dev", "Member A", Set.of(UserRole.REGISTERED_USER));
        UserEntity memberB = saveUser("member-b@hackhub.dev", "Member B", Set.of(UserRole.REGISTERED_USER));

        TeamEntity teamA = saveTeam("Alpha", 4, Set.of(memberA));
        TeamEntity teamB = saveTeam("Beta", 4, Set.of(memberB));

        HackathonEntity hackathon = saveHackathon(
            "HackHub Finals",
            organizer,
            judge,
            HackathonStateType.IN_VALUTAZIONE,
            ScoringPolicyType.DEFAULT,
            ValidationPolicyType.BASIC
        );

        registrationRepository.save(RegistrationEntity.builder().hackathon(hackathon).team(teamA).build());
        registrationRepository.save(RegistrationEntity.builder().hackathon(hackathon).team(teamB).build());

        SubmissionEntity subA = submissionRepository.save(SubmissionEntity.builder()
            .hackathon(hackathon)
            .team(teamA)
            .repoUrl("https://github.com/org/alpha")
            .description("Alpha project description long enough for validation")
            .updatedAt(LocalDateTime.now().minusHours(2))
            .status(SubmissionStatus.VALIDATED)
            .build());

        SubmissionEntity subB = submissionRepository.save(SubmissionEntity.builder()
            .hackathon(hackathon)
            .team(teamB)
            .repoUrl("https://github.com/org/beta")
            .description("Beta project description long enough for validation")
            .updatedAt(LocalDateTime.now().minusHours(1))
            .status(SubmissionStatus.VALIDATED)
            .build());

        evaluationRepository.save(EvaluationEntity.builder()
            .hackathon(hackathon)
            .submission(subA)
            .judge(judge)
            .score0to10(new BigDecimal("8.20"))
            .comment("Strong")
            .build());

        evaluationRepository.save(EvaluationEntity.builder()
            .hackathon(hackathon)
            .submission(subB)
            .judge(judge)
            .score0to10(new BigDecimal("9.10"))
            .comment("Excellent")
            .build());

        MvcResult result = mockMvc.perform(post("/api/hackathons/{hackathonId}/declare-winner", hackathon.getId())
                .header("X-USER-ID", organizer.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hackathonId").value(hackathon.getId()))
            .andExpect(jsonPath("$.teamId").value(teamB.getId()))
            .andExpect(jsonPath("$.paymentTxId", startsWith("pay-")))
            .andReturn();

        WinnerEntity winner = winnerRepository.findByHackathon_Id(hackathon.getId()).orElseThrow();
        HackathonEntity updated = hackathonRepository.findById(hackathon.getId()).orElseThrow();

        assertEquals(teamB.getId(), winner.getTeam().getId());
        assertEquals(HackathonStateType.CONCLUSO, updated.getStateEnum());

        String payload = result.getResponse().getContentAsString();
        Map<?, ?> parsed = objectMapper.readValue(payload, Map.class);
        assertTrue(parsed.get("paymentTxId").toString().startsWith("pay-"));
    }

    @Test
    void submitSubmission_endpointBlockedWhenHackathonNotRunning() throws Exception {
        UserEntity organizer = saveUser("org2@hackhub.dev", "Organizer 2", Set.of(UserRole.ORGANIZER));
        UserEntity judge = saveUser("judge2@hackhub.dev", "Judge 2", Set.of(UserRole.JUDGE));
        UserEntity member = saveUser("member@hackhub.dev", "Member", Set.of(UserRole.REGISTERED_USER));

        TeamEntity team = saveTeam("Gamma", 3, Set.of(member));

        HackathonEntity hackathon = saveHackathon(
            "HackHub Qualifiers",
            organizer,
            judge,
            HackathonStateType.ISCRIZIONI,
            ScoringPolicyType.DEFAULT,
            ValidationPolicyType.REPO_REQUIRED
        );

        registrationRepository.save(RegistrationEntity.builder().hackathon(hackathon).team(team).build());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("repoUrl", "https://github.com/org/gamma");
        requestBody.put("description", "Long enough description for repo required policy.");

        mockMvc.perform(post("/api/hackathons/{hackathonId}/submissions", hackathon.getId())
                .header("X-USER-ID", member.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.title").value("ForbiddenActionForState"));
    }

    @Test
    void deleteHackathon_endpointDeletesHackathonAndRelatedData() throws Exception {
        UserEntity organizer = saveUser("org-delete@hackhub.dev", "Organizer Delete", Set.of(UserRole.ORGANIZER));
        UserEntity judge = saveUser("judge-delete@hackhub.dev", "Judge Delete", Set.of(UserRole.JUDGE));
        UserEntity mentor = saveUser("mentor-delete@hackhub.dev", "Mentor Delete", Set.of(UserRole.MENTOR));
        UserEntity member = saveUser("member-delete@hackhub.dev", "Member Delete", Set.of(UserRole.REGISTERED_USER));

        TeamEntity team = saveTeam("DeleteTeam", 4, Set.of(member));
        HackathonEntity hackathon = saveHackathon(
            "Hackathon To Delete",
            organizer,
            judge,
            HackathonStateType.IN_VALUTAZIONE,
            ScoringPolicyType.DEFAULT,
            ValidationPolicyType.BASIC
        );
        hackathon.getMentors().add(mentor);
        hackathon = hackathonRepository.save(hackathon);

        registrationRepository.save(RegistrationEntity.builder().hackathon(hackathon).team(team).build());

        SubmissionEntity submission = submissionRepository.save(SubmissionEntity.builder()
            .hackathon(hackathon)
            .team(team)
            .repoUrl("https://github.com/org/delete-team")
            .description("Submission to be deleted")
            .updatedAt(LocalDateTime.now())
            .status(SubmissionStatus.SUBMITTED)
            .build());

        evaluationRepository.save(EvaluationEntity.builder()
            .hackathon(hackathon)
            .submission(submission)
            .judge(judge)
            .score0to10(new BigDecimal("7.50"))
            .comment("To be removed")
            .build());

        supportRequestRepository.save(SupportRequestEntity.builder()
            .hackathon(hackathon)
            .team(team)
            .message("Need help")
            .status(SupportRequestStatus.OPEN)
            .build());

        callProposalRepository.save(CallProposalEntity.builder()
            .hackathon(hackathon)
            .team(team)
            .mentor(mentor)
            .proposedSlots("2026-03-10T10:00,2026-03-10T11:00")
            .status(CallProposalStatus.PROPOSED)
            .build());

        violationReportRepository.save(ViolationReportEntity.builder()
            .hackathon(hackathon)
            .team(team)
            .mentor(mentor)
            .reason("Violation for deletion test")
            .build());

        WinnerEntity winner = hackathonService.declareWinner(hackathon.getId(), organizer.getId());

        mockMvc.perform(delete("/api/hackathons/{hackathonId}", hackathon.getId())
                .header("X-USER-ID", organizer.getId()))
            .andExpect(status().isNoContent());

        assertTrue(hackathonRepository.findById(hackathon.getId()).isEmpty());
        assertTrue(registrationRepository.findByHackathon_Id(hackathon.getId()).isEmpty());
        assertTrue(submissionRepository.findByHackathon_Id(hackathon.getId()).isEmpty());
        assertTrue(evaluationRepository.findByHackathon_Id(hackathon.getId()).isEmpty());
        assertTrue(supportRequestRepository.findByHackathon_Id(hackathon.getId()).isEmpty());
        assertTrue(callProposalRepository.findByHackathon_Id(hackathon.getId()).isEmpty());
        assertTrue(violationReportRepository.findByHackathon_Id(hackathon.getId()).isEmpty());
        assertTrue(!winnerRepository.existsByHackathon_Id(winner.getHackathonId()));
    }

    private UserEntity saveUser(String email, String name, Set<UserRole> roles) {
        UserEntity user = UserEntity.builder()
            .email(email)
            .name(name)
            .roles(new HashSet<>(roles))
            .build();
        return userRepository.save(user);
    }

    private TeamEntity saveTeam(String name, int maxSize, Set<UserEntity> members) {
        TeamEntity team = TeamEntity.builder()
            .name(name)
            .maxSize(maxSize)
            .members(new HashSet<>(members))
            .build();
        return teamRepository.save(team);
    }

    private HackathonEntity saveHackathon(
        String name,
        UserEntity organizer,
        UserEntity judge,
        HackathonStateType state,
        ScoringPolicyType scoringPolicy,
        ValidationPolicyType validationPolicy
    ) {
        LocalDateTime now = LocalDateTime.now();
        HackathonEntity hackathon = HackathonEntity.builder()
            .name(name)
            .rules("Rules")
            .registrationDeadline(now.plusDays(1))
            .startDate(now.plusDays(2))
            .endDate(now.plusDays(3))
            .location("Milan")
            .prizeMoney(new BigDecimal("1000.00"))
            .maxTeamSize(5)
            .stateEnum(state)
            .scoringPolicyType(scoringPolicy)
            .validationPolicyType(validationPolicy)
            .organizer(organizer)
            .judge(judge)
            .mentors(new HashSet<>())
            .build();
        return hackathonRepository.save(hackathon);
    }

    private void cleanupAll() {
        winnerRepository.deleteAll();
        evaluationRepository.deleteAll();
        callProposalRepository.deleteAll();
        supportRequestRepository.deleteAll();
        violationReportRepository.deleteAll();
        submissionRepository.deleteAll();
        registrationRepository.deleteAll();
        hackathonRepository.deleteAll();
        teamRepository.deleteAll();
        userRepository.deleteAll();
    }
}
