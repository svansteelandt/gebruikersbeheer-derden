package be.vdab.gebruikersbeheer.derden.domain;


import be.vdab.gebruikersbeheer.util.common.domain.Dn;

import java.io.Serializable;

public class SystemUserObject implements Serializable {

	private String uid;
	private Dn owner;
	private Dn dn;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public Dn getOwner() {
		return owner;
	}

	public void setOwner(Dn owner) {
		this.owner = owner;
	}

	public Dn getIsimDn() {
		return this.dn;
	}

	public void setIsimDn(Dn dn) {
		this.dn = dn;
	}
}
