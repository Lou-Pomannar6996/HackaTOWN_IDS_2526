package it.ids.hackathown.api.controller;

import it.ids.hackathown.api.dto.request.AddMentorRequest;
import it.ids.hackathown.api.dto.request.AggiornaStatoRequest;
import it.ids.hackathown.api.dto.request.CreateHackathonRequest;
import it.ids.hackathown.api.dto.response.AggiornaStatoFormResponse;
import it.ids.hackathown.api.dto.response.ApiResponse;
import it.ids.hackathown.api.dto.response.HackathonResponse;
import it.ids.hackathown.api.dto.response.TeamResponse;
import it.ids.hackathown.api.mapper.ApiMapper;
import it.ids.hackathown.domain.entity.Hackathon;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.service.HackathonService;
import it.ids.hackathown.service.dto.AggiornaStatoFormDTO;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hackathons")
@RequiredArgsConstructor
public class HackathonController {

    private final HackathonService hackathonService;
    private final ApiMapper mapper;

    @GetMapping
    public List<HackathonResponse> getElencoHackathon(@RequestParam(required = false) String filtri) {
        String normalized = filtri == null ? "" : filtri;
        return hackathonService.listaHackathonPubblici(normalized).stream().map(mapper::toResponse).toList();
    }

    @PostMapping
    public ResponseEntity<HackathonResponse> creaHackathon(
        @RequestHeader(HeaderConstants.USER_ID) Integer currentUserId,
        @Valid @RequestBody CreateHackathonRequest request
    ) {
        if (request == null) {
            throw new DomainValidationException("Dati hackathon non validi");
        }
        Hackathon dati = Hackathon.builder()
            .nome(request.name())
            .descrizione(request.description())
            .regolamento(request.rules())
            .scadenzaIscrizioni(request.registrationDeadline())
            .dataInizio(request.startDate())
            .dataFine(request.endDate())
            .luogo(request.location())
            .premio(request.prizeMoney())
            .maxTeamSize(request.maxTeamSize())
            .build();
        HackathonResponse response = mapper.toResponse(hackathonService.creaHackathon(
            dati,
            requirePositiveId(currentUserId, "Organizzatore"),
            request.giudiceId(),
            request.mentoriIds()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{hackathonId}/teams/{teamId}/registrations")
    public ResponseEntity<Void> iscriviTeam(
        @PathVariable Integer hackathonId,
        @PathVariable Integer teamId
    ) {
        hackathonService.iscriviTeam(
            requirePositiveId(hackathonId, "Hackathon"),
            requirePositiveId(teamId, "Team")
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{hackathonId}/mentors")
    public ResponseEntity<Void> aggiungiMentore(
        @PathVariable Integer hackathonId,
        @Valid @RequestBody AddMentorRequest request
    ) {
        hackathonService.aggiungiMentore(
            requirePositiveId(hackathonId, "Hackathon"),
            requirePositiveId(request.mentorUserId(), "Mentore")
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{hackathonId}/teams/{teamId}/prize")
    public ResponseEntity<String> erogaPremio(
        @PathVariable Integer hackathonId,
        @PathVariable Integer teamId
    ) {
        String result = hackathonService.erogaPremio(
            requirePositiveId(hackathonId, "Hackathon"),
            requirePositiveId(teamId, "Team")
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{hackathonId}/start")
    public ResponseEntity<ApiResponse> avviaHackathon(
        @PathVariable Integer hackathonId,
        @RequestHeader(HeaderConstants.USER_ID) Integer currentUserId
    ) {
        hackathonService.avviaHackathon(
            requirePositiveId(hackathonId, "Hackathon"),
            requirePositiveId(currentUserId, "Organizzatore")
        );
        return ResponseEntity.ok(new ApiResponse("Hackathon avviato", null));
    }

    @PostMapping("/{hackathonId}/start-evaluation")
    public ResponseEntity<ApiResponse> avviaValutazione(
        @PathVariable Integer hackathonId,
        @RequestHeader(HeaderConstants.USER_ID) Integer currentUserId
    ) {
        hackathonService.avviaValutazione(
            requirePositiveId(hackathonId, "Hackathon"),
            requirePositiveId(currentUserId, "Organizzatore")
        );
        return ResponseEntity.ok(new ApiResponse("Valutazione avviata", null));
    }

    @GetMapping("/{hackathonId}")
    public HackathonResponse getDettaglioHackathon(@PathVariable Integer hackathonId) {
        Hackathon hackathon = hackathonService.getDettaglioHackathon(requirePositiveId(hackathonId, "Hackathon"));
        return hackathon == null ? null : mapper.toResponse(hackathon);
    }

    @GetMapping("/{hackathonId}/status/form")
    public AggiornaStatoFormResponse getFormAggiornaStato(
        @PathVariable Integer hackathonId,
        @RequestHeader(HeaderConstants.USER_ID) Integer currentUserId
    ) {
        AggiornaStatoFormDTO form = hackathonService.getFormAggiornaStato(
            requirePositiveId(hackathonId, "Hackathon"),
            requirePositiveId(currentUserId, "Organizzatore")
        );
        return new AggiornaStatoFormResponse(form.statoCorrente(), form.transizioniPossibili());
    }

    @PutMapping("/{hackathonId}/status")
    public ResponseEntity<ApiResponse> aggiornaStato(
        @PathVariable Integer hackathonId,
        @RequestHeader(HeaderConstants.USER_ID) Integer currentUserId,
        @Valid @RequestBody AggiornaStatoRequest request
    ) {
        if (request == null || request.nuovoStato() == null || request.nuovoStato().isBlank()) {
            throw new DomainValidationException("Nuovo stato non valido");
        }
        hackathonService.aggiornaStato(
            requirePositiveId(hackathonId, "Hackathon"),
            requirePositiveId(currentUserId, "Organizzatore"),
            request.nuovoStato()
        );
        return ResponseEntity.ok(new ApiResponse("Stato aggiornato", null));
    }

    @PostMapping("/{hackathonId}/conclude")
    public ResponseEntity<ApiResponse> concludiHackathon(
        @PathVariable Integer hackathonId,
        @RequestHeader(HeaderConstants.USER_ID) Integer currentUserId
    ) {
        hackathonService.concludiHackathon(
            requirePositiveId(hackathonId, "Hackathon"),
            requirePositiveId(currentUserId, "Organizzatore")
        );
        return ResponseEntity.ok(new ApiResponse("Hackathon concluso", null));
    }

    @GetMapping("/{hackathonId}/winner/candidates")
    public List<TeamResponse> preparaProclama(
        @PathVariable Integer hackathonId,
        @RequestHeader(HeaderConstants.USER_ID) Integer currentUserId
    ) {
        return hackathonService.preparaProclama(
            requirePositiveId(currentUserId, "Organizzatore"),
            requirePositiveId(hackathonId, "Hackathon")
        ).stream().map(mapper::toResponse).toList();
    }

    @PostMapping("/{hackathonId}/winner/{teamId}")
    public ResponseEntity<Void> proclamaVincitore(
        @PathVariable Integer hackathonId,
        @PathVariable Integer teamId,
        @RequestHeader(HeaderConstants.USER_ID) Integer currentUserId
    ) {
        hackathonService.proclamaVincitore(
            requirePositiveId(currentUserId, "Organizzatore"),
            requirePositiveId(hackathonId, "Hackathon"),
            requirePositiveId(teamId, "Team")
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private Integer requirePositiveId(Integer value, String label) {
        if (value == null || value <= 0) {
            throw new DomainValidationException(label + " non valido");
        }
        return value;
    }
}
