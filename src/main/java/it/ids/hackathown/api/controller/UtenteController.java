package it.ids.hackathown.api.controller;

import it.ids.hackathown.api.dto.request.RegisterUserRequest;
import it.ids.hackathown.service.UtenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UtenteController {

    private final UtenteService utenteService;

    @PostMapping
    public ResponseEntity<Void> registraUtente(@Valid @RequestBody RegisterUserRequest request) {
        utenteService.registraUtente(request.email(), request.password(), request.nome(), request.cognome());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
