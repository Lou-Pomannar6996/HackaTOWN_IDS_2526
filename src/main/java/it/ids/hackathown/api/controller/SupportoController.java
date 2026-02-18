package it.ids.hackathown.api.controller;

import it.ids.hackathown.api.dto.request.CreateSupportRequest;
import it.ids.hackathown.api.dto.request.ProposeCallRequest;
import it.ids.hackathown.api.dto.response.CallProposalResponse;
import it.ids.hackathown.api.dto.response.SupportRequestResponse;
import it.ids.hackathown.api.mapper.ApiMapper;
import it.ids.hackathown.service.SupportoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SupportoController {

    private final SupportoService supportoService;
    private final ApiMapper mapper;

    @PostMapping("/hackathons/{hackathonId}/support-requests")
    public ResponseEntity<SupportRequestResponse> inviaRichiestaSupporto(
        @PathVariable Long hackathonId,
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId,
        @Valid @RequestBody CreateSupportRequest request
    ) {
        SupportRequestResponse response = mapper.toResponse(
            supportoService.createSupportRequest(hackathonId, currentUserId, request.message())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/hackathons/{hackathonId}/support-requests")
    public List<SupportRequestResponse> getRichieste(
        @PathVariable Long hackathonId,
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId
    ) {
        return supportoService.listSupportRequests(hackathonId, currentUserId).stream().map(mapper::toResponse).toList();
    }

    @PostMapping("/support-requests/{supportRequestId}/propose-call")
    public ResponseEntity<CallProposalResponse> pianificaCall(
        @PathVariable Long supportRequestId,
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId,
        @Valid @RequestBody ProposeCallRequest request
    ) {
        CallProposalResponse response = mapper.toResponse(
            supportoService.proposeCall(supportRequestId, currentUserId, request.proposedSlots())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public List<SupportRequestResponse> getRichiesteSupportoMentore(Long hackathonId, Long currentUserId) {
        return supportoService.listSupportRequests(hackathonId, currentUserId).stream().map(mapper::toResponse).toList();
    }
}
