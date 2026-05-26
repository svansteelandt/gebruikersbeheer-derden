package be.vdab.gebruikersbeheer.derden.security;

import be.vdab.gebruikersbeheer.derden.domain.IsimUserData;
import org.springframework.core.NamedThreadLocal;

public final class IsimUserContextHolder {

	private static final ThreadLocal<IsimUserData> isimUserDataHolder = new NamedThreadLocal<>("Isim UserData");

	private IsimUserContextHolder() {
	}

	public static IsimUserData getContext() {
		return isimUserDataHolder.get();
	}

	static void setContext(IsimUserData isimUserData) {
		if (isimUserData == null) {
			isimUserDataHolder.remove();
		} else {
			isimUserDataHolder.set(isimUserData);
		}
	}

	public static void clearContext() {
		isimUserDataHolder.remove();
	}
}
