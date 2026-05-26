package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;

public interface AdminService {
	void createNewAdmin(AdminDomainObject adminDomain, PersonObject adminPerson);
	void deleteAdmin(AdminDomainObject adminDomain, PersonObject adminPerson);
	void deleteFromAdminDomainCache(Object key);
}
