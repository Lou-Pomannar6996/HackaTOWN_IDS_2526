package it.ids.hackathown.api.controller;

import it.ids.hackathown.api.dto.request.LoginRequest;
import it.ids.hackathown.api.dto.response.UserResponse;
import it.ids.hackathown.api.mapper.ApiMapper;
import it.ids.hackathown.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final ApiMapper mapper;

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request) {
        return mapper.toResponse(authenticationService.login(request.email(), request.password()));
    }
}
