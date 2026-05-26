package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.exception.PersonNotFoundException;
import be.vdab.gebruikersbeheer.derden.exception.VestigingNietGevondenException;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonRestoreServiceTest {

	@Mock
	PersonService personService;
	@Mock
	ApplicationProperties applicationProperties;
	@Mock
	AdminDomainService adminDomainService;
	@Mock
	AccountService accountService;

	@InjectMocks
	PersonRestoreServiceImpl personRestoreService;

	@Test
	@DisplayName("""
			GIVEN vestiging id does not exist
			WHEN restore user is called
			THEN VestigingNietGevondenException will be thrown
			""")
	void willThrowVestigingNotFoundExceptionIfVestigingDoesNotExist() {
		var person = new PersonObject();
		person.setIkp(Ikp.of(203000L));
		var gebruikerDn = Dn.of("erglobalid=1,");
		when(applicationProperties.createPersonDn(gebruikerDn.getGlobalId())).thenReturn(gebruikerDn);
		when(personService.findPersonByDn(gebruikerDn, null)).thenReturn(Optional.of(person));
		when(adminDomainService.findAdminDomainByIkp(person.getIkp())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> personRestoreService.restore(gebruikerDn.getGlobalId())).isInstanceOf(VestigingNietGevondenException.class);
	}

	@Test
	@DisplayName("""
			GIVEN person id does not exist
			WHEN restore user is called
			THEN PersonNotFoundException will be thrown
			""")
	void willThrowPersonNotFoundExceptionIfPersonDoesNotExist() {
		var person = new PersonObject();
		person.setIkp(Ikp.of(203000L));
		var gebruikerDn = Dn.of("erglobalid=1,");
		when(applicationProperties.createPersonDn(gebruikerDn.getGlobalId())).thenReturn(gebruikerDn);
		when(personService.findPersonByDn(gebruikerDn, null)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> personRestoreService.restore(gebruikerDn.getGlobalId())).isInstanceOf(PersonNotFoundException.class);
	}

	@Test
	@DisplayName("""
			GIVEN valid person id, vestiging of person exists and person has email
			WHEN restore user is called
			THEN person is restored and password reset
			""")
	void restorePersonWithEmail() {
		var person = new PersonObject();
		person.setIkp(Ikp.of(203000L));
		person.setUserId("uid");
		person.setEmailAddress("a@a.com");
		var gebruikerDn = Dn.of("erglobalid=1,");
		when(applicationProperties.createPersonDn(gebruikerDn.getGlobalId())).thenReturn(gebruikerDn);
		when(personService.findPersonByDn(gebruikerDn, null)).thenReturn(Optional.of(person));
		when(adminDomainService.findAdminDomainByIkp(person.getIkp())).thenReturn(Optional.of(new AdminDomainObject()));
		when(personService.restorePerson(person)).thenReturn(true);

		personRestoreService.restore(gebruikerDn.getGlobalId());

		verify(personService).restorePerson(person);
		verify(accountService).resetPassword(person.getUserId(), person.getEmailAddress());
	}

	@Test
	@DisplayName("""
			GIVEN valid person id, vestiging of person exists and person has email
			WHEN restore user is called but failes
			THEN false is returned an no password reset
			""")
	void restoreForPersonWithEmailFailes() {
		var person = new PersonObject();
		person.setIkp(Ikp.of(203000L));
		person.setUserId("uid");
		person.setEmailAddress("a@a.com");
		var gebruikerDn = Dn.of("erglobalid=1,");
		when(applicationProperties.createPersonDn(gebruikerDn.getGlobalId())).thenReturn(gebruikerDn);
		when(personService.findPersonByDn(gebruikerDn, null)).thenReturn(Optional.of(person));
		when(adminDomainService.findAdminDomainByIkp(person.getIkp())).thenReturn(Optional.of(new AdminDomainObject()));
		when(personService.restorePerson(person)).thenReturn(false);

		personRestoreService.restore(gebruikerDn.getGlobalId());

		verify(personService).restorePerson(person);
		verifyNoInteractions(accountService);
	}

	@Test
	@DisplayName("""
			GIVEN valid person id, vestiging of person exists and person has NO email
			WHEN restore user is called
			THEN person is restored and password is NOT reset
			""")
	void restorePersonWithoutEmail() {
		var person = new PersonObject();
		person.setIkp(Ikp.of(203000L));
		person.setUserId("uid");
		var gebruikerDn = Dn.of("erglobalid=1,");
		when(applicationProperties.createPersonDn(gebruikerDn.getGlobalId())).thenReturn(gebruikerDn);
		when(personService.findPersonByDn(gebruikerDn, null)).thenReturn(Optional.of(person));
		when(adminDomainService.findAdminDomainByIkp(person.getIkp())).thenReturn(Optional.of(new AdminDomainObject()));
		when(personService.restorePerson(person)).thenReturn(true);

		personRestoreService.restore(gebruikerDn.getGlobalId());

		verify(personService).restorePerson(person);
		verifyNoInteractions(accountService);
	}
}