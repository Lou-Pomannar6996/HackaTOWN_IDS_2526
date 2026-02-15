package it.ids.hackathown.api.dto.request;

import it.ids.hackathown.domain.enums.UserRole;
import java.util.Set;

public record UpdateUserRolesRequest(Set<UserRole> roles) {
}
