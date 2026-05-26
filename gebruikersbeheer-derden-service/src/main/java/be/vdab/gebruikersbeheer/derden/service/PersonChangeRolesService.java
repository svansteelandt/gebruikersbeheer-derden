package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.domain.RoleAssignmentResult;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface PersonChangeRolesService {
	RoleAssignmentResult changeRoles(@NotNull String personGlobalId, @NotNull List<String> roleIds, String csvRole);
}
