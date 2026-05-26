package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.exception.GebruikerBestaatReedsOpDezeVestigingException;
import be.vdab.gebruikersbeheer.derden.exception.GebruikerCreateFoutException;
import be.vdab.gebruikersbeheer.derden.exception.PersonCreateValidationException;
import be.vdab.gebruikersbeheer.derden.exception.RijksRegisternummerIsReedsIngebruikException;
import be.vdab.gebruikersbeheer.derden.exception.VestigingNietGevondenException;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface PersonCreateService {

	PersonObject create(@NotNull String vestigingGlobalId,
	                    @NotNull PersonObject person,
	                    @NotNull List<RoleObject> rolesToAdd) throws PersonCreateValidationException,
			VestigingNietGevondenException,
			RijksRegisternummerIsReedsIngebruikException,
			GebruikerBestaatReedsOpDezeVestigingException,
			GebruikerCreateFoutException;
}
