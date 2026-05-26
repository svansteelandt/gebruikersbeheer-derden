package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.domain.PersonSearch;
import be.vdab.gebruikersbeheer.derden.domain.PersonsZoekResultaat;
import jakarta.validation.constraints.NotNull;

public interface PersonSearchService {

	PersonsZoekResultaat zoek(@NotNull PersonSearch searchCommand);
}
