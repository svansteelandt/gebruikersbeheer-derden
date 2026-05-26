package be.vdab.gebruikersbeheer.derden.intern.command;

import be.vdab.gebruikersbeheer.derden.core.domain.Code;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PersonCommand implements Serializable {

	@Serial
	private static final long serialVersionUID = 7135342430165049029L;

	private List<String> rolesByPerson;
	private List<RoleObject> roles;
	private PersonObject person;
	private String trigger;
	private List<Code> cvsRollen;

	public PersonCommand() {
		// default cstr voor spring
	}

	public PersonCommand(PersonObject person) {
		super();
		this.person = person;
	}

	public PersonCommand(PersonObject person, List<Code> cvsRollen) {
		super();
		this.person = person;
		this.cvsRollen = cvsRollen;
	}

	public PersonCommand(List<RoleObject> roles) {
		super();
		this.roles = roles;
	}

	public List<RoleObject> getHasRoles() {
		List<RoleObject> roleObjects = new ArrayList<RoleObject>();
		if (this.roles != null) {
			for (RoleObject roleObject : this.roles) {
				if (roleObject.getHasRole()) {
					roleObjects.add(roleObject);
				}
			}
		}
		return roleObjects;
	}

	public List<RoleObject> getRolesNoAdmin() {
		if (roles != null) {
			for (RoleObject roleObject : roles) {
				if (roleObject.isAdminRole()) {
					roles.remove(roleObject);
					break;
				}
			}
		}

		return roles;
	}

	public List<RoleObject> getRoles() {
		return roles;
	}

	public void setRoles(List<RoleObject> roles) {
		this.roles = roles;
	}

	public List<String> getRolesByPerson() {
		return rolesByPerson;
	}

	public void setRolesByPerson(List<String> rolesByPerson) {
		this.rolesByPerson = rolesByPerson;
	}

	public PersonObject getPerson() {
		return person;
	}

	public void setPerson(PersonObject person) {
		this.person = person;
	}

	public String getTrigger() {
		return trigger;
	}

	public void setTrigger(String trigger) {
		this.trigger = trigger;
	}

	public List<Code> getCvsRollen() {
		return cvsRollen;
	}

	public void setCvsRollen(List<Code> cvsRollen) {
		this.cvsRollen = cvsRollen;
	}
}