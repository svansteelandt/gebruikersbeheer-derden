package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.mapper;

import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.CvsCodeDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.GebruikerDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.GebruikerStatus;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.GebruikerSummaryDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands.CreateGebruikerCommand;
import be.vdab.gebruikersbeheer.derden.core.CodesPort;
import be.vdab.gebruikersbeheer.derden.core.domain.Code;
import be.vdab.gebruikersbeheer.derden.domain.LoginMethod;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonStatus;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GebruikerMapper {

	private final CodesPort codesPort;

	public List<GebruikerSummaryDto> map(List<PersonObject> personObjects) {
		return personObjects.stream()
				.map(this::toGebruikerSummary
				).toList();
	}

	public GebruikerSummaryDto toGebruikerSummary(PersonObject person) {
		return new GebruikerSummaryDto(
				person.getDn().getGlobalId(),
				person.getUserId(),
				person.getVdabUid(),
				person.getFullName(),
				person.getProfileName(),
				person.getDisplayRole(),
				person.isSuspend(),
				LoginMethod.fromValue(person.getLoginMethod()),
				person.getRoles().stream().map(RoleObject::getGlobalId).toList(),
				person.getBedrijfsnaam(),
				person.getIkp() != null ? person.getIkp().getDisplayValue() : null,
				toGebruikerStatus(person.getStatus()),
				person.getDeleteDescription(),
				person.getParentGlobalId(),
				person.getEmailAddress()
		);
	}

	public GebruikerDto toGebruiker(PersonObject person) {
		return new GebruikerDto(
				person.getDn().getGlobalId(),
				person.getParentGlobalId(),
				person.getUserId(),
				person.getVdabUid(),
				person.getFullName(),
				person.getFirstName(),
				person.getLastName(),
				person.getProfileName(),
				person.getDisplayRole(),
				person.isSuspend(),
				LoginMethod.fromValue(person.getLoginMethod()),
				person.getRoles().stream().filter(RoleObject::getHasRole).toList(),
				person.isRrnAccessible(),
				person.isRrnAccessible() ? person.getNationalNumber() : null,
				person.getEmailAddress(),
				person.getPhone(),
				person.getMobile(),
				person.getIkp().getDisplayValue(),
				person.getBedrijfsnaam(),
				person.getSuspendOmschrijving(),
				getCvsRol(person.getVdabCvsRol(), codesPort.getCVSRollen())
		);
	}

	private CvsCodeDto getCvsRol(String vdabCvsRol, List<Code> cvsRolCodes) {
		if (vdabCvsRol != null) {
			return cvsRolCodes.stream()
					.filter(code -> vdabCvsRol.equals(code.getWaarde()))
					.findFirst()
					.map(code -> new CvsCodeDto(code.getWaarde(), code.getLangLabel()))
					.orElse(new CvsCodeDto(vdabCvsRol, vdabCvsRol));
		}
		return null;
	}

	public PersonObject mapToPerson(CreateGebruikerCommand createGebruikerCommand) {
		var person = new PersonObject();
		person.setFirstName(createGebruikerCommand.firstName());
		person.setLastName(createGebruikerCommand.lastName());
		person.setPhone(createGebruikerCommand.phone());
		person.setMobile(createGebruikerCommand.mobile());
		person.setNoRrn(createGebruikerCommand.hasNoRrnr());
		if (!person.isNoRrn()) {
			person.setNationalNumber(createGebruikerCommand.rrnr());
		} else {
			person.setNationalNumber(""); // anders isim fout
		}
		person.setEmailAddress(createGebruikerCommand.email());
		return person;
	}

	private GebruikerStatus toGebruikerStatus(PersonStatus personStatus) {
		return switch (personStatus) {
			case ACTIVE -> GebruikerStatus.ACTIEF;
			case INACTIVE -> GebruikerStatus.PASSIEF;
			case SUSPENDED -> GebruikerStatus.GESCHORST;
		};
	}
}
