package be.vdab.gebruikersbeheer.derden.util.isim.support;

public interface ContextManager {

	void init();
	void startPrivilegedSession();

	void endPrivilegedSession();

	void clearSessions();
}
