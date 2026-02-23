package it.ids.hackathown.api.controller;

import it.ids.hackathown.api.dto.request.ProponiCallRequest;
import it.ids.hackathown.service.SupportoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calls")
@RequiredArgsConstructor
public class CallController {

    private final SupportoService supportoService;

    @PostMapping
    public ResponseEntity<Void> proponiCall(@Valid @RequestBody ProponiCallRequest request) {
        supportoService.proponiCall(
            request.mentorId(),
            request.richiestaId(),
            request.dataProposta(),
            request.durataMin(),
            request.calendarEventId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
