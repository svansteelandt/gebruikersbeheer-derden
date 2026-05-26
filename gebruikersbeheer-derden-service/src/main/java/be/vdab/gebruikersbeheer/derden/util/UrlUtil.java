package be.vdab.gebruikersbeheer.derden.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UrlUtil {

	public static String urlEncode(String text) {
		if (text == null) {
			return null;
		}
		return URLEncoder.encode(text, StandardCharsets.UTF_8);
	}

	public static String urlDecode(String text) {
		if (text == null) {
			return null;
		}
		return URLDecoder.decode(text, StandardCharsets.UTF_8);
	}
}
