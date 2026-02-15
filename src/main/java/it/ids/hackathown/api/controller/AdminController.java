package it.ids.hackathown.api.controller;

import it.ids.hackathown.api.dto.request.UpdateUserRolesRequest;
import it.ids.hackathown.api.dto.response.UserResponse;
import it.ids.hackathown.api.mapper.ApiMapper;
import it.ids.hackathown.service.UserService;
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

    private final UserService userService;
    private final ApiMapper mapper;

    @PutMapping("/users/{userId}/roles")
    public UserResponse updateUserRoles(
        @RequestHeader(HeaderConstants.USER_ID) Long currentUserId,
        @PathVariable Long userId,
        @RequestBody UpdateUserRolesRequest request
    ) {
        return mapper.toResponse(userService.assignRoles(currentUserId, userId, request.roles()));
    }
}
