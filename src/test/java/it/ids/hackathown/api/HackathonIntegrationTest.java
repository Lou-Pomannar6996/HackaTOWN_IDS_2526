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
import it.ids.hackathown.domain.entity.SegnalaViolazione;
import it.ids.hackathown.domain.entity.EsitoHackathon;
import it.ids.hackathown.domain.enums.ScoringPolicyType;
import it.ids.hackathown.domain.enums.StatoCall;
import it.ids.hackathown.domain.enums.StatoHackathon;
import it.ids.hackathown.domain.enums.StatoRichiesta;
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
import java.time.ZoneId;
import java.util.Date;
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

        Hackathon hackathon = saveHackathon("HackHub Finals", StatoHackathon.IN_VALUTAZIONE);

        Iscrizione regA = iscrizioneRepository.save(Iscrizione.builder().hackathon(hackathon).team(teamA).build());
        Iscrizione regB = iscrizioneRepository.save(Iscrizione.builder().hackathon(hackathon).team(teamB).build());

        Sottomissione subA = sottomissioneRepository.save(Sottomissione.builder()
            .iscrizione(regA)
            .urlRepo("https://github.com/org/alpha")
            .descrizione("Alpha project description long enough for validation")
            .dataInvio(new Date())
            .dataUltimoAggiornamento(Date.from(LocalDateTime.now().minusHours(2).atZone(ZoneId.systemDefault()).toInstant()))
            .build());

        Sottomissione subB = sottomissioneRepository.save(Sottomissione.builder()
            .iscrizione(regB)
            .urlRepo("https://github.com/org/beta")
            .descrizione("Beta project description long enough for validation")
            .dataInvio(new Date())
            .dataUltimoAggiornamento(Date.from(LocalDateTime.now().minusHours(1).atZone(ZoneId.systemDefault()).toInstant()))
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

        EsitoHackathon winner = esitoHackathonRepository.findByHackathon_Id(hackathon.getId().longValue()).orElseThrow();
        Hackathon updated = hackathonRepository.findById(hackathon.getId().longValue()).orElseThrow();

        assertEquals(teamB.getId(), winner.getTeam().getId());
        assertEquals(StatoHackathon.CONCLUSO, updated.getStato());

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

        Hackathon hackathon = saveHackathon("HackHub Qualifiers", StatoHackathon.ISCRIZIONI);

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
        Hackathon hackathon = saveHackathon("Hackathon To Delete", StatoHackathon.IN_VALUTAZIONE);

        Iscrizione registration = iscrizioneRepository.save(Iscrizione.builder().hackathon(hackathon).team(team).build());

        Sottomissione submission = sottomissioneRepository.save(Sottomissione.builder()
            .iscrizione(registration)
            .urlRepo("https://github.com/org/delete-team")
            .descrizione("Submission to be deleted")
            .dataInvio(new Date())
            .dataUltimoAggiornamento(new Date())
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
            .stato(StatoRichiesta.APERTA)
            .build());

        callSupportoRepository.save(CallSupporto.builder()
            .dataInizio(Date.from(LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant()))
            .durataMin(30)
            .stato(StatoCall.PROPOSTA)
            .build());

        segnalazioneValidazioneRepository.save(SegnalaViolazione.builder()
            .hackathon(hackathon)
            .mentore(mentor)
            .motivazione("Violation for deletion test")
            .build());

        EsitoHackathon winner = hackathonService.declareWinner(
            hackathon.getId().longValue(),
            organizer.getId().longValue()
        );

        mockMvc.perform(delete("/api/hackathons/{hackathonId}", hackathon.getId())
                .header("X-USER-ID", organizer.getId()))
            .andExpect(status().isNoContent());

        Long hackathonId = hackathon.getId().longValue();
        assertTrue(hackathonRepository.findById(hackathonId).isEmpty());
        assertTrue(iscrizioneRepository.findByHackathon_Id(hackathonId).isEmpty());
        assertTrue(sottomissioneRepository.findByIscrizione_Hackathon_Id(hackathonId).isEmpty());
        assertTrue(valutazioneRepository.findByHackathon_Id(hackathonId).isEmpty());
        assertTrue(richiestaSupportoRepository.findByHackathon_Id(hackathonId).isEmpty());
        assertTrue(segnalazioneValidazioneRepository.findByHackathon_Id(hackathonId).isEmpty());
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

    private Hackathon saveHackathon(String name, StatoHackathon state) {
        LocalDateTime now = LocalDateTime.now();
        Date registrationDeadline = Date.from(now.plusDays(1).atZone(ZoneId.systemDefault()).toInstant());
        Date startDate = Date.from(now.plusDays(2).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(now.plusDays(3).atZone(ZoneId.systemDefault()).toInstant());
        Hackathon hackathon = Hackathon.builder()
            .nome(name)
            .descrizione("Test description")
            .regolamento("Rules")
            .scadenzaIscrizioni(registrationDeadline)
            .dataInizio(startDate)
            .dataFine(endDate)
            .luogo("Milan")
            .premio(new BigDecimal("1000.00"))
            .maxTeamSize(5)
            .stato(state)
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
