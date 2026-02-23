package it.ids.hackathown.api.dto.request;

import it.ids.hackathown.domain.enums.UserRole;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record UpdateUserRolesRequest(@NotEmpty Set<UserRole> roles) {
}
