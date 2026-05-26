package be.vdab.gebruikersbeheer.derden.domain;

import be.vdab.gebruikersbeheer.derden.enumeration.Profile;
import be.vdab.gebruikersbeheer.derden.util.ValidateUtils;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonObject implements Serializable {

	private static final Pattern NEWLINE_PATTERN = Pattern.compile("[\\r\\n]+");

	public static final int STATUS_INACTIVE = 1;
	public static final int STATUS_ACTIVE = 0;

	@EqualsAndHashCode.Include
	private Dn dn;

	/**
	 * Doesn't contain the actual profile. Actual values are either vdabintern, vdabderde, vdabvirtual or null.
	 */
	private String profileName;
	private String userId;
	private String firstName;
	private String lastName;
	private String nationalNumber;
	private String emailAddress;
	private String commonName;
	private String phone;
	private String mobile;
	private Ikp ikp;
	private String vdabUid;
	private String vdabCvsRol;
	private boolean suspend;

	@Setter(AccessLevel.NONE) // voorkomt dat Lombok een setter genereert
	private String suspendOmschrijving;
	private boolean hasRole;
	private boolean changed;
	private boolean pending;
	private boolean noRrn;
	private long employeenumber;
	private String employeeType;
	private PersonStatus status;
	private Dn parentDn;
	private List<RoleObject> roles = new ArrayList<>();
	private boolean disabled;
	@Setter(AccessLevel.PRIVATE)
	private boolean rrnAccessible = false;
	private boolean virtualAccount = false;
	private String loginMethod;
	private String bedrijfsnaam;
	private String deleteDescription;
	private String vdaboe;

	public PersonObject() {
		this.profileName = Profile.PERSON.getType();
	}

	public PersonObject(Dn dn) {
		this.dn = dn;
		this.profileName = Profile.PERSON.getType();
	}

	public PersonObject(PersonObject original) {
		this.dn = original.dn;
		this.profileName = original.profileName;
		this.userId = original.userId;
		this.firstName = original.firstName;
		this.lastName = original.lastName;
		this.nationalNumber = original.nationalNumber;
		this.emailAddress = original.emailAddress;
		this.commonName = original.commonName;
		this.phone = original.phone;
		this.mobile = original.mobile;
		this.ikp = original.ikp;
		this.vdabUid = original.vdabUid;
		this.vdabCvsRol = original.vdabCvsRol;
		this.suspend = original.suspend;
		this.suspendOmschrijving = original.suspendOmschrijving;
		this.hasRole = original.hasRole;
		this.changed = original.changed;
		this.pending = original.pending;
		this.noRrn = original.noRrn;
		this.employeenumber = original.employeenumber;
		this.employeeType = original.employeeType;
		this.status = original.status;
		this.parentDn = original.parentDn;
		this.disabled = original.disabled;
		this.rrnAccessible = original.rrnAccessible;
		this.virtualAccount = original.virtualAccount;
		this.loginMethod = original.loginMethod;
		this.bedrijfsnaam = original.bedrijfsnaam;
		this.vdaboe = original.vdaboe;
		this.deleteDescription = original.deleteDescription;

		this.roles = original.getRoles() != null ? new ArrayList<>(original.getRoles()) : new ArrayList<>();
	}

	// this is used in the jsp pages - even if there is no explicit getter used
	public String getDisplayRole() {
		if (roles == null) {
			return "";
		}

		return roles.stream()
				.filter(RoleObject::getHasRole)
				.filter(role -> !role.isPending()) //pending roles mogen niet voorkomen in overzicht
				.map(RoleObject::getVdabRoleName)
				.filter(StringUtils::isNotBlank)
				.collect(Collectors.joining(", "));
	}

	public String getFullName() {
		if (this.firstName == null && this.lastName == null) {
			return null;
		}

		return Stream.of(this.firstName, this.lastName)
				.filter(StringUtils::isNotBlank)
				.collect(Collectors.joining(" "));
	}

	public String getNationalNumberFormatted() {
		if (ValidateUtils.isValidInsz(nationalNumber)) {
			return "%s.%s.%s-%s.%s".formatted(nationalNumber.substring(0, 2), nationalNumber.substring(2, 4), nationalNumber.substring(4, 6), nationalNumber.substring(6, 9), nationalNumber.substring(9, 11));
		} else if (nationalNumber != null) {
			return nationalNumber;
		} else {
			return "";
		}
	}

	public void setNationalNumber(String nationalNumber) {
		this.nationalNumber = nationalNumber.replaceAll("\\D", "");
		rrnAccessible = nationalNumber.length() != 9; //auto generated id number
	}

	public boolean hasRole(String roleName) {
		if (roles == null) {
			return false;
		}
		return roles.stream().anyMatch(r -> r.getRoleName().equals(roleName));
	}

	public void setHasRole(boolean hasRole) {
		if (this.hasRole != hasRole) {
			this.changed = !this.changed;
		}
		this.hasRole = hasRole;
	}

	public boolean isActive() {
		return status == PersonStatus.ACTIVE;
	}

	public boolean isInactive() {
		return !isActive();
	}

	// this is used in the jsp pages - even if there is no explicit getter used
	public String getParentGlobalId() {
		return this.parentDn.getGlobalId();
	}

	public boolean getHasRole() {
		return hasRole;
	}

	public String getDeleteDescription() {
		return StringUtils.isNotEmpty(deleteDescription) ? deleteDescription : "Onbekend";
	}

	public void setSuspendOmschrijving(String suspendOmschrijving) {
		if (StringUtils.isNotEmpty(suspendOmschrijving)) {
			this.suspendOmschrijving = NEWLINE_PATTERN
					.matcher(suspendOmschrijving.trim())
					.replaceAll(" - ");
		} else {
			this.suspendOmschrijving = suspendOmschrijving;
		}
	}
}