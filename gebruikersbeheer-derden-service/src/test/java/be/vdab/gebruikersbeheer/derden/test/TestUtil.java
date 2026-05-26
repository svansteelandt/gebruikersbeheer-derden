package be.vdab.gebruikersbeheer.derden.test;


import be.vdab.gebruikersbeheer.util.common.domain.Dn;

public class TestUtil {

	private static long counter = 12345678L;

	private TestUtil() {}

	public static Dn isimDn(String erGlobalId) {
		return Dn.of("erGlobalId=%s,cn=test".formatted(erGlobalId));
	}

	public static Dn uniqueIsimDn(String reason) {
		return Dn.of("erGlobalId=%d,cn=%s".formatted(counter++, reason));
	}
}
