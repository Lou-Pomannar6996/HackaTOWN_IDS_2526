package it.ids.hackathown.api.controller;

import it.ids.hackathown.api.dto.request.UpdateUserRolesRequest;
import it.ids.hackathown.api.dto.response.UserResponse;
import it.ids.hackathown.api.mapper.ApiMapper;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.service.UtenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UtenteService utenteService;
    private final ApiMapper mapper;

    @PutMapping("/users/{userId}/roles")
    public UserResponse updateUserRoles(
            @RequestHeader(HeaderConstants.USER_ID) Long currentUserId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRolesRequest request
    ) {
        Integer adminId = requirePositiveId(currentUserId, "Admin");
        Integer targetId = requirePositiveId(userId, "Utente");
        return mapper.toResponse(utenteService.assignRoles(adminId.longValue(), targetId.longValue(), request.roles()));
    }

    private Integer requirePositiveId(Long value, String label) {
        if (value == null || value <= 0) {
            throw new DomainValidationException(label + " non valido");
        }
        return value.intValue();
    }
}
