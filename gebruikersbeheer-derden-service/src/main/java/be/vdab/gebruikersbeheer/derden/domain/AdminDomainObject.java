package be.vdab.gebruikersbeheer.derden.domain;

import be.vdab.gebruikersbeheer.derden.util.LogSanitizer;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class AdminDomainObject implements Serializable {

	private String name;
	private String street;
	private String city;
	private String postalcode;
	private Dn parent;
	private String globalId;
	private String ou;
	private String vdaboe;
	private String oeName;
	private Ikp ikp;
	private String kboNummer;

	private Boolean headQuarter;
	private Dn dn;

	private Set<String> samakks;
	private List<RoleObject> roles = new ArrayList<>();
	private List<PersonObject> administrators = new ArrayList<>();

	public String getVdaboe() {
		return vdaboe;
	}

	public void setVdaboe(String vdaboe) {
		this.vdaboe = vdaboe;
	}

	public String getOu() {
		return ou;
	}

	public void setOu(String ou) {
		this.ou = ou;
	}

	public String getGlobalId() {
		return globalId;
	}

	public void setGlobalId(String globalId) {
		this.globalId = globalId;
	}

	public Dn getParent() {
		return parent;
	}

	public void setParent(Dn parent) {
		this.parent = parent;
	}

	public AdminDomainObject() {
		super();
	}

	public AdminDomainObject(Dn dn) {
		this.dn = dn;
	}

	@NonNull
	public List<PersonObject> getAdministrators() {
		return administrators;
	}

	public void setAdministrators(@NonNull List<PersonObject> administrators) {
		this.administrators = administrators;
	}

	@NonNull
	public List<RoleObject> getRoles() {
		return roles;
	}

	public void setRoles( List<RoleObject> roles) {
		this.roles = roles != null ? new ArrayList<>(roles) : new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPostalcode() {
		return postalcode;
	}

	public void setPostalcode(String postalcode) {
		this.postalcode = postalcode;
	}

	public Boolean getHeadQuarter() {
		return headQuarter;
	}

	public void setHeadQuarter(Boolean headQuarter) {
		this.headQuarter = headQuarter;
	}

	public Ikp getIkp() {
		return ikp;
	}

	public String getIkpIntern() {
		return ikp != null ? ikp.getDisplayValue() : "";
	}

	public void setIkp(Ikp ikp) {
		this.ikp = ikp;
	}

	public String getKboNummer() {
		return this.kboNummer;
	}

	public void setKboNummer(String kboNummer) {
		this.kboNummer = kboNummer;
	}

	public Dn getDn() {
		return dn;
	}

	public void setDn(Dn dn) {
		this.dn = dn;
	}

	public Set<String> getSamakks() {
		return samakks;
	}

	public void setSamakks(Set<String> samakks) {
		this.samakks = samakks;
	}

	public String getAddress() {
		return "%s %s %s".formatted(this.street, this.postalcode, this.city);
	}

	public String getOeName() {
		return oeName;
	}

	public void setOeName(String oeName) {
		this.oeName = oeName;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(250);
		buf.append("AdminDomainObject name=");
		buf.append(name);
		buf.append(" street=");
		buf.append(street);
		buf.append(" city=");
		buf.append(city);
		buf.append(" postalcode=");
		buf.append(postalcode);
		buf.append("\nparent=");
		buf.append(parent);
		buf.append("\nuid=");
		buf.append(globalId);
		buf.append(" Ou=");
		buf.append(ou);
		buf.append(" vdaboe=");
		buf.append(vdaboe);
		buf.append(" ikp=");
		buf.append(ikp);
		if (this.kboNummer != null) {
			buf.append(" vdabkbo=");
			buf.append(this.kboNummer);
		}
		buf.append(" headQuarter=");
		buf.append(headQuarter);
		buf.append(" dn=");
		buf.append(dn);
		if (roles != null) {
			buf.append("\nroles=");
			int index = 0;
			for (RoleObject role : roles) {
				if (index > 0) {
					buf.append(",");
				}
				buf.append(role.getRoleName());
				index++;
			}
		} else {
			buf.append(" roles=/");
		}
		buf.append("\nadministrators=");

		int index = 0;
		for (PersonObject admin : administrators) {
			if (index > 0) {
				buf.append(",");
			}
			buf.append(admin.getCommonName());
			index++;
		}

		return buf.toString();
	}

	public void addAdministrator(PersonObject personObject) {
		if (log.isDebugEnabled()) {
			log.debug("addAdministrator {} to {}", LogSanitizer.sanitize(personObject.getUserId()), name);
		}
		this.administrators.add(personObject);
	}

	public void deleteAdministrator(PersonObject personObject) {
		Set<PersonObject> adminToDelete = this.administrators.stream()
				.filter(p -> p.getDn().equals(personObject.getDn()))
				.collect(Collectors.toSet());
		if (log.isDebugEnabled()) {
			log.debug("deleteAdministrator {} from {}", LogSanitizer.sanitize(personObject.getUserId()), name);
		}
		this.administrators.removeAll(adminToDelete);
	}

	public List<RoleObject> getPossibleRolesForPerson() {
		List<RoleObject> possiblePersonRoles= getRoles().stream().map(RoleObject::new).toList();

		List<PersonObject> admins = getAdministrators().stream().filter(p -> !p.isVirtualAccount()).toList();

		if (admins.isEmpty()) {
			possiblePersonRoles.stream()
					.filter(RoleObject::isAdminRole)
					.findFirst()
					.ifPresent(roleObject -> roleObject.setHasRole(true));
		}

		return possiblePersonRoles;
	}
}
