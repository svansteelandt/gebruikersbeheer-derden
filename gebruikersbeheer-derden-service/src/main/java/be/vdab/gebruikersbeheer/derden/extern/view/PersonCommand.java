package be.vdab.gebruikersbeheer.derden.extern.view;

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

	public PersonCommand(){
		// default cstr voor spring
	}

	public PersonCommand(PersonObject person) {
		this.person = person;
	}

	public PersonCommand(List<RoleObject> roles) {
		this.roles = roles;
	}

	public List<RoleObject> getHasRoles() {
		List<RoleObject> roleObjects = new ArrayList<>();
		if (this.roles != null) {
			for (RoleObject roleObject : this.roles) {
				if (roleObject.getHasRole()) {
					roleObjects.add(roleObject);
				}
			}
		}
		return roleObjects;
	}

	public List<RoleObject> getRoles() {
		return roles;
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
}