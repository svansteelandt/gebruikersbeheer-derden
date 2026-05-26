package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.exception.MaximumAantalAdminsOverschredenException;
import be.vdab.gebruikersbeheer.derden.exception.MinimumAantalAdminsNietGehaaldException;

import java.util.List;

public interface AssignUsersToRolService {

	void assign(String globalIdOfOrganisation, String globalIdOfRole, List<String> personWhoGetTheRole) throws MinimumAantalAdminsNietGehaaldException, MaximumAantalAdminsOverschredenException;
}
