package be.vdab.gebruikersbeheer.derden.domain;


import be.vdab.gebruikersbeheer.util.common.domain.Dn;

import java.io.Serializable;

public class OrganizationalUnitObject implements Serializable {

	private Dn dn;
	private String name;

	public OrganizationalUnitObject(Dn dn) {
		super();
		this.dn = dn;
	}

	public OrganizationalUnitObject(Dn dn, String name) {
		super();
		this.dn = dn;
		this.name = name;
	}

	public Dn getDn() {
		return dn;
	}

	public void setDn(Dn dn) {
		this.dn = dn;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "OrganizationalUnitObject "  + "\n" +
				"["  + "\n" +
				"dn=" + dn + "\n" +
				"name=" + name  + "\n" +
				"]";
	}

	
}