package it.ids.hackathown.api.controller;

import it.ids.hackathown.api.dto.request.LoginRequest;
import it.ids.hackathown.api.dto.response.UserResponse;
import it.ids.hackathown.api.mapper.ApiMapper;
import it.ids.hackathown.service.AutenticazioneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AutenticazioneController {

    private final AutenticazioneService autenticazioneService;
    private final ApiMapper mapper;

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request) {
        return mapper.toResponse(autenticazioneService.login(request.email(), request.password()));
    }
}
