package it.ids.hackathown.api.controller;

import it.ids.hackathown.api.dto.request.UpsertSubmissionRequest;
import it.ids.hackathown.api.dto.response.SubmissionResponse;
import it.ids.hackathown.api.mapper.ApiMapper;
import it.ids.hackathown.service.SubmissionService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final ApiMapper mapper;

    @PostMapping("/hackathons/{hackathonId}/submissions")
    public ResponseEntity<SubmissionResponse> submit(
        @PathVariable Long hackathonId,
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId,
        @RequestBody UpsertSubmissionRequest request
    ) {
        SubmissionResponse response = mapper.toResponse(submissionService.submitSubmission(
            hackathonId,
            currentUserId,
            request.title(),
            request.description(),
            request.repoUrl()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/submissions/{submissionId}")
    public SubmissionResponse updateSubmission(
        @PathVariable Long submissionId,
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId,
        @RequestBody UpsertSubmissionRequest request
    ) {
        return mapper.toResponse(submissionService.updateSubmission(
            submissionId,
            currentUserId,
            request.title(),
            request.description(),
            request.repoUrl()
        ));
    }

    @GetMapping("/hackathons/{hackathonId}/submissions")
    public List<SubmissionResponse> listSubmissions(
        @PathVariable Long hackathonId,
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId
    ) {
        return submissionService.listSubmissions(hackathonId, currentUserId).stream().map(mapper::toResponse).toList();
    }
}
