package be.vdab.gebruikersbeheer.derden.extern.view;

import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class RoleCommand implements Serializable {

	@Serial
	private static final long serialVersionUID = -6123668079581105577L;

	private RoleObject roleObject;
	private List<PersonObject> personObject;
	
	public RoleCommand(RoleObject roleObject, List<PersonObject> personObject) {
		super();
		this.roleObject = roleObject;
		this.personObject = personObject;
	}

	public RoleCommand(List<PersonObject> personObject) {
		super();
		this.personObject = personObject;
	}

	public RoleCommand() {
		super();
	}

	public RoleObject getRoleObject() {
		return roleObject;
	}

	public void setRole(RoleObject roleObject) {
		this.roleObject = roleObject;
	}

	public List<PersonObject> getPersonObject() {
		return personObject;
	}

	public void setPersonObject(List<PersonObject> personObject) {
		this.personObject = personObject;
	}
}