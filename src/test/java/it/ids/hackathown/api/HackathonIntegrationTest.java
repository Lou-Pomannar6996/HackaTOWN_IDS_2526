package it.ids.hackathown.api;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.ids.hackathown.domain.entity.CallSupporto;
import it.ids.hackathown.domain.entity.Valutazione;
import it.ids.hackathown.domain.entity.Hackathon;
import it.ids.hackathown.domain.entity.Iscrizione;
import it.ids.hackathown.domain.entity.Sottomissione;
import it.ids.hackathown.domain.entity.RichiestaSupporto;
import it.ids.hackathown.domain.entity.Team;
import it.ids.hackathown.domain.entity.Utente;
import it.ids.hackathown.domain.entity.SegnalazioneViolazione;
import it.ids.hackathown.domain.entity.EsitoHackathon;
import it.ids.hackathown.domain.enums.CallProposalStatus;
import it.ids.hackathown.domain.enums.HackathonStateType;
import it.ids.hackathown.domain.enums.ScoringPolicyType;
import it.ids.hackathown.domain.enums.SubmissionStatus;
import it.ids.hackathown.domain.enums.SupportRequestStatus;
import it.ids.hackathown.domain.enums.UserRole;
import it.ids.hackathown.domain.enums.ValidationPolicyType;
import it.ids.hackathown.repository.CallSupportoRepository;
import it.ids.hackathown.repository.ValutazioneRepository;
import it.ids.hackathown.repository.HackathonRepository;
import it.ids.hackathown.repository.IscrizioneRepository;
import it.ids.hackathown.repository.SottomissioneRepository;
import it.ids.hackathown.repository.RichiestaSupportoRepository;
import it.ids.hackathown.repository.TeamRepository;
import it.ids.hackathown.repository.UtenteRepository;
import it.ids.hackathown.repository.SegnalazioneValidazioneRepository;
import it.ids.hackathown.repository.EsitoHackathonRepository;
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
    private UtenteRepository utenteRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private HackathonRepository hackathonRepository;

    @Autowired
    private IscrizioneRepository iscrizioneRepository;

    @Autowired
    private SottomissioneRepository sottomissioneRepository;

    @Autowired
    private ValutazioneRepository valutazioneRepository;

    @Autowired
    private EsitoHackathonRepository esitoHackathonRepository;

    @Autowired
    private RichiestaSupportoRepository richiestaSupportoRepository;

    @Autowired
    private CallSupportoRepository callSupportoRepository;

    @Autowired
    private SegnalazioneValidazioneRepository segnalazioneValidazioneRepository;

    @Autowired
    private HackathonService hackathonService;

    @BeforeEach
    void beforeEach() {
        cleanupAll();
    }

    @Test
    void declareWinner_endpointSelectsBestSubmissionAndCompletesHackathon() throws Exception {
        Utente organizer = saveUser("organizer@hackhub.dev", "Organizer", Set.of(UserRole.ORGANIZER));
        Utente judge = saveUser("judge@hackhub.dev", "Judge", Set.of(UserRole.JUDGE));
        Utente memberA = saveUser("member-a@hackhub.dev", "Member A", Set.of(UserRole.REGISTERED_USER));
        Utente memberB = saveUser("member-b@hackhub.dev", "Member B", Set.of(UserRole.REGISTERED_USER));

        Team teamA = saveTeam("Alpha", 4, Set.of(memberA));
        Team teamB = saveTeam("Beta", 4, Set.of(memberB));

        Hackathon hackathon = saveHackathon(
            "HackHub Finals",
            organizer,
            judge,
            HackathonStateType.IN_VALUTAZIONE,
            ScoringPolicyType.DEFAULT,
            ValidationPolicyType.BASIC
        );

        iscrizioneRepository.save(Iscrizione.builder().hackathon(hackathon).team(teamA).build());
        iscrizioneRepository.save(Iscrizione.builder().hackathon(hackathon).team(teamB).build());

        Sottomissione subA = sottomissioneRepository.save(Sottomissione.builder()
            .hackathon(hackathon)
            .team(teamA)
            .urlRepo("https://github.com/org/alpha")
            .descrizione("Alpha project description long enough for validation")
            .dataUltimoAggiornamento(LocalDateTime.now().minusHours(2))
            .status(SubmissionStatus.VALIDATED)
            .build());

        Sottomissione subB = sottomissioneRepository.save(Sottomissione.builder()
            .hackathon(hackathon)
            .team(teamB)
            .urlRepo("https://github.com/org/beta")
            .descrizione("Beta project description long enough for validation")
            .dataUltimoAggiornamento(LocalDateTime.now().minusHours(1))
            .status(SubmissionStatus.VALIDATED)
            .build());

        valutazioneRepository.save(Valutazione.builder()
            .hackathon(hackathon)
            .submission(subA)
            .judge(judge)
            .punteggio(new BigDecimal("8.20"))
            .giudizio("Strong")
            .build());

        valutazioneRepository.save(Valutazione.builder()
            .hackathon(hackathon)
            .submission(subB)
            .judge(judge)
            .punteggio(new BigDecimal("9.10"))
            .giudizio("Excellent")
            .build());

        MvcResult result = mockMvc.perform(post("/api/hackathons/{hackathonId}/declare-winner", hackathon.getId())
                .header("X-USER-ID", organizer.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hackathonId").value(hackathon.getId()))
            .andExpect(jsonPath("$.teamId").value(teamB.getId()))
            .andExpect(jsonPath("$.paymentTxId", startsWith("pay-")))
            .andReturn();

        EsitoHackathon winner = esitoHackathonRepository.findByHackathon_Id(hackathon.getId()).orElseThrow();
        Hackathon updated = hackathonRepository.findById(hackathon.getId()).orElseThrow();

        assertEquals(teamB.getId(), winner.getTeam().getId());
        assertEquals(HackathonStateType.CONCLUSO, updated.getStato());

        String payload = result.getResponse().getContentAsString();
        Map<?, ?> parsed = objectMapper.readValue(payload, Map.class);
        assertTrue(parsed.get("paymentTxId").toString().startsWith("pay-"));
    }

    @Test
    void submitSubmission_endpointBlockedWhenHackathonNotRunning() throws Exception {
        Utente organizer = saveUser("org2@hackhub.dev", "Organizer 2", Set.of(UserRole.ORGANIZER));
        Utente judge = saveUser("judge2@hackhub.dev", "Judge 2", Set.of(UserRole.JUDGE));
        Utente member = saveUser("member@hackhub.dev", "Member", Set.of(UserRole.REGISTERED_USER));

        Team team = saveTeam("Gamma", 3, Set.of(member));

        Hackathon hackathon = saveHackathon(
            "HackHub Qualifiers",
            organizer,
            judge,
            HackathonStateType.ISCRIZIONI,
            ScoringPolicyType.DEFAULT,
            ValidationPolicyType.REPO_REQUIRED
        );

        iscrizioneRepository.save(Iscrizione.builder().hackathon(hackathon).team(team).build());

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
        Utente organizer = saveUser("org-delete@hackhub.dev", "Organizer Delete", Set.of(UserRole.ORGANIZER));
        Utente judge = saveUser("judge-delete@hackhub.dev", "Judge Delete", Set.of(UserRole.JUDGE));
        Utente mentor = saveUser("mentor-delete@hackhub.dev", "Mentor Delete", Set.of(UserRole.MENTOR));
        Utente member = saveUser("member-delete@hackhub.dev", "Member Delete", Set.of(UserRole.REGISTERED_USER));

        Team team = saveTeam("DeleteTeam", 4, Set.of(member));
        Hackathon hackathon = saveHackathon(
            "Hackathon To Delete",
            organizer,
            judge,
            HackathonStateType.IN_VALUTAZIONE,
            ScoringPolicyType.DEFAULT,
            ValidationPolicyType.BASIC
        );
        hackathon.getMentors().add(mentor);
        hackathon = hackathonRepository.save(hackathon);

        iscrizioneRepository.save(Iscrizione.builder().hackathon(hackathon).team(team).build());

        Sottomissione submission = sottomissioneRepository.save(Sottomissione.builder()
            .hackathon(hackathon)
            .team(team)
            .urlRepo("https://github.com/org/delete-team")
            .descrizione("Submission to be deleted")
            .dataUltimoAggiornamento(LocalDateTime.now())
            .status(SubmissionStatus.SUBMITTED)
            .build());

        valutazioneRepository.save(Valutazione.builder()
            .hackathon(hackathon)
            .submission(submission)
            .judge(judge)
            .punteggio(new BigDecimal("7.50"))
            .giudizio("To be removed")
            .build());

        richiestaSupportoRepository.save(RichiestaSupporto.builder()
            .hackathon(hackathon)
            .team(team)
            .descrizione("Need help")
            .stato(SupportRequestStatus.OPEN)
            .build());

        callSupportoRepository.save(CallSupporto.builder()
            .hackathon(hackathon)
            .team(team)
            .mentor(mentor)
            .dataInizio(LocalDateTime.now().plusDays(1))
            .durataMin(30)
            .stato(CallProposalStatus.PROPOSED)
            .build());

        segnalazioneValidazioneRepository.save(SegnalazioneViolazione.builder()
            .hackathon(hackathon)
            .team(team)
            .mentor(mentor)
            .motivaizone("Violation for deletion test")
            .build());

        EsitoHackathon winner = hackathonService.declareWinner(hackathon.getId(), organizer.getId());

        mockMvc.perform(delete("/api/hackathons/{hackathonId}", hackathon.getId())
                .header("X-USER-ID", organizer.getId()))
            .andExpect(status().isNoContent());

        assertTrue(hackathonRepository.findById(hackathon.getId()).isEmpty());
        assertTrue(iscrizioneRepository.findByHackathon_Id(hackathon.getId()).isEmpty());
        assertTrue(sottomissioneRepository.findByHackathon_Id(hackathon.getId()).isEmpty());
        assertTrue(valutazioneRepository.findByHackathon_Id(hackathon.getId()).isEmpty());
        assertTrue(richiestaSupportoRepository.findByHackathon_Id(hackathon.getId()).isEmpty());
        assertTrue(callSupportoRepository.findByHackathon_Id(hackathon.getId()).isEmpty());
        assertTrue(segnalazioneValidazioneRepository.findByHackathon_Id(hackathon.getId()).isEmpty());
        assertTrue(!esitoHackathonRepository.existsByHackathon_Id(winner.getId()));
    }

    private Utente saveUser(String email, String nome, Set<UserRole> roles) {
        Utente user = Utente.builder()
            .email(email)
            .nome(nome)
            .cognome("Test")
            .password("pwd")
            .roles(new HashSet<>(roles))
            .build();
        return utenteRepository.save(user);
    }

    private Team saveTeam(String name, int maxSize, Set<Utente> members) {
        Team team = Team.builder()
            .nome(name)
            .maxMembri(maxSize)
            .membri(new HashSet<>(members))
            .build();
        return teamRepository.save(team);
    }

    private Hackathon saveHackathon(
        String name,
        Utente organizer,
        Utente judge,
        HackathonStateType state,
        ScoringPolicyType scoringPolicy,
        ValidationPolicyType validationPolicy
    ) {
        LocalDateTime now = LocalDateTime.now();
        Hackathon hackathon = Hackathon.builder()
            .nome(name)
            .regolamento("Rules")
            .scadenzaIscrizioni(now.plusDays(1))
            .dataInizio(now.plusDays(2))
            .dataFine(now.plusDays(3))
            .luogo("Milan")
            .premio(new BigDecimal("1000.00"))
            .maxTeamSize(5)
            .stato(state)
            .scoringPolicyType(scoringPolicy)
            .validationPolicyType(validationPolicy)
            .organizer(organizer)
            .judge(judge)
            .mentors(new HashSet<>())
            .build();
        return hackathonRepository.save(hackathon);
    }

    private void cleanupAll() {
        esitoHackathonRepository.deleteAll();
        valutazioneRepository.deleteAll();
        callSupportoRepository.deleteAll();
        richiestaSupportoRepository.deleteAll();
        segnalazioneValidazioneRepository.deleteAll();
        sottomissioneRepository.deleteAll();
        iscrizioneRepository.deleteAll();
        hackathonRepository.deleteAll();
        teamRepository.deleteAll();
        utenteRepository.deleteAll();
    }
}
