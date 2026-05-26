package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.domain.GebruikerExportData;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapManager;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapPerson;
import com.opencsv.ICSVWriter;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

import static be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames.DEFAULT_PERSON_ATTRIBUTES_DERDEN;

@Service
public class CsvExportServiceImpl implements CsvExportService {

	private final IsimLdapManager isimLdapManager;
	private final RoleService roleService;

	public CsvExportServiceImpl(IsimLdapManager isimLdapManager, RoleService roleService) {
		this.isimLdapManager = isimLdapManager;
		this.roleService = roleService;
	}

	@Override
	public void sendCsv(HttpServletResponse response, Ikp ikp, String fileName) throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		var gebruikers = isimLdapManager.getPersonsInOrganizationAndSuborganisations(ikp, DEFAULT_PERSON_ATTRIBUTES_DERDEN)
				.stream()
				.filter(this::isDerde)
				.map(this::mapToExportData)
				.toList();

		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
		response.setContentType("text/csv; charset=utf-8");
		var writer = new StatefulBeanToCsvBuilder<GebruikerExportData>(response.getWriter())
				.withQuotechar(ICSVWriter.DEFAULT_QUOTE_CHARACTER)
				.withSeparator(ICSVWriter.DEFAULT_SEPARATOR)
				.withOrderedResults(true)
				.build();
		writer.write(new GebruikerExportData("NAAM", "VOORNAAM", "GEBRUIKERSNAAM", "VESTIGINGSNUMMER", "BEDRIJFSNAAM", "TOEGANGSRECHTEN", "TELEFOON", "EMAIL"));
		writer.write(gebruikers);
	}

	private boolean isDerde(IsimLdapPerson person) {
		return "derde".equalsIgnoreCase(person.getEmployeeType());
	}

	private GebruikerExportData mapToExportData(IsimLdapPerson person) {
		return new GebruikerExportData(person.getNaam(),
				person.getVoornaam(),
				person.getVdabUid(),
				String.valueOf(person.getIkp()),
				person.getBedrijfsnaam(),
				person.getRollen().stream()
						.map(roleService::findRoleByDN)
						.flatMap(Optional::stream)
						.map(RoleObject::getVdabRoleName)
						.collect(Collectors.joining(",")),
				person.getTelefoon(),
				person.getEmail());
	}
}
