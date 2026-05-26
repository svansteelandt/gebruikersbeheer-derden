package be.vdab.gebruikersbeheer.derden.domain;

public record RoleAssignmentResult(boolean someRolesAreChanged, boolean minAdminsReached, boolean maxAdminsReached) {
}
