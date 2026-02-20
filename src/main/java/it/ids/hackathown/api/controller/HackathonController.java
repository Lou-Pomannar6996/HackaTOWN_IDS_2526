package it.ids.hackathown.api.controller;

import it.ids.hackathown.api.dto.request.AddMentorRequest;
import it.ids.hackathown.api.dto.request.CreateHackathonRequest;
import it.ids.hackathown.api.dto.response.HackathonResponse;
import it.ids.hackathown.api.dto.response.WinnerResponse;
import it.ids.hackathown.api.mapper.ApiMapper;
import it.ids.hackathown.service.HackathonService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hackathons")
@RequiredArgsConstructor
public class HackathonController {

    private final HackathonService hackathonService;
    private final ApiMapper mapper;

    @PostMapping
    public ResponseEntity<HackathonResponse> creaHackathon(
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId,
        @Valid @RequestBody CreateHackathonRequest request
    ) {
        HackathonResponse response = mapper.toResponse(hackathonService.createHackathon(
            currentUserId,
            request.name(),
            request.description(),
            request.rules(),
            request.registrationDeadline(),
            request.startDate(),
            request.endDate(),
            request.location(),
            request.prizeMoney(),
            request.maxTeamSize()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<HackathonResponse> getElencoHackathon() {
        return hackathonService.listHackathons().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{hackathonId}")
    public HackathonResponse getDettaglioHackathon(@PathVariable Long hackathonId) {
        return mapper.toResponse(hackathonService.getHackathon(hackathonId));
    }

    @PostMapping("/{hackathonId}/mentors")
    public HackathonResponse aggiungiMentore(
        @PathVariable Long hackathonId,
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId,
        @Valid @RequestBody AddMentorRequest request
    ) {
        return mapper.toResponse(hackathonService.addMentor(hackathonId, request.mentorUserId(), currentUserId));
    }

    @PostMapping("/{hackathonId}/start")
    public HackathonResponse startHackathon(
        @PathVariable Long hackathonId,
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId
    ) {
        return mapper.toResponse(hackathonService.startHackathon(hackathonId, currentUserId));
    }

    @PostMapping("/{hackathonId}/start-evaluation")
    public HackathonResponse startEvaluation(
        @PathVariable Long hackathonId,
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId
    ) {
        return mapper.toResponse(hackathonService.startEvaluation(hackathonId, currentUserId));
    }

    @PostMapping("/{hackathonId}/declare-winner")
    public WinnerResponse proclamaVincitore(
        @PathVariable Long hackathonId,
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId
    ) {
        return mapper.toResponse(hackathonService.declareWinner(hackathonId, currentUserId));
    }

    @DeleteMapping("/{hackathonId}")
    public ResponseEntity<Void> deleteHackathon(
        @PathVariable Long hackathonId,
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId
    ) {
        hackathonService.deleteHackathon(hackathonId, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
