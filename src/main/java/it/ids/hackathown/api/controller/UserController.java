package it.ids.hackathown.api.controller;

import it.ids.hackathown.api.dto.request.RegisterUserRequest;
import it.ids.hackathown.api.dto.response.UserResponse;
import it.ids.hackathown.api.mapper.ApiMapper;
import it.ids.hackathown.service.UserService;
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
public class UserController {

    private final UserService userService;
    private final ApiMapper mapper;

    @PostMapping
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterUserRequest request) {
        UserResponse response = mapper.toResponse(
            userService.registerUser(request.email(), request.password(), request.nome(), request.cognome())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
