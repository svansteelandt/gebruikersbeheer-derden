package be.vdab.gebruikersbeheer.derden.core;

import be.vdab.gebruikersbeheer.derden.core.domain.Code;

import java.util.List;

public interface CodesPort {

	List<Code> getCVSRollen();

	List<Code> getWebcursussen();

	List<String> getAdditionalTimRollen();
}
