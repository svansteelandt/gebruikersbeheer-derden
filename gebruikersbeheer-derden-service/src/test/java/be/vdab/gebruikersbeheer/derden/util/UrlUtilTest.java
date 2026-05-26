package be.vdab.gebruikersbeheer.derden.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UrlUtilTest {

	@Test
	void urlEncode() {
		assertThat(UrlUtil.urlEncode(null)).isNull();
		assertThat(UrlUtil.urlEncode("{enc}hallo")).isEqualTo("%7Benc%7Dhallo");
	}

	@Test
	void urlDecode() {
		assertThat(UrlUtil.urlDecode(null)).isNull();
		assertThat(UrlUtil.urlDecode("%7Benc%7Dhallo")).isEqualTo("{enc}hallo");
	}
}