package it.ids.hackathown.api.dto.response;

import it.ids.hackathown.domain.enums.UserRole;
import java.util.Set;

public record UserResponse(
    Long id,
    String email,
    String name,
    Set<UserRole> roles
) {
}
