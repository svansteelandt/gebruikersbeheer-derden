package be.vdab.gebruikersbeheer.derden.security;


import jakarta.servlet.http.Cookie;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SameSiteCookieCsrfTokenRepositoryTest {
    static final Random RANDOM = new Random();
    static final String SET_COOKIE = "Set-Cookie";
    static final String SECURE = "Secure";
    static final String HTTP_ONLY = "HttpOnly";
    static final String EXPIRES = "Expires";

    @InjectMocks
	SameSiteCookieCsrfTokenRepository csrfTokenRepository;
    @Mock
    Environment environment;

    MockHttpServletRequest request;
    MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void generateToken() {
        CsrfToken csrfToken = csrfTokenRepository.generateToken(request);

        assertThat(csrfToken).isInstanceOf(DefaultCsrfToken.class);
        assertThat(csrfToken.getHeaderName()).isEqualTo(SameSiteCookieCsrfTokenRepository.DEFAULT_CSRF_HEADER_NAME);
        assertThat(csrfToken.getParameterName()).isEqualTo(SameSiteCookieCsrfTokenRepository.DEFAULT_CSRF_PARAMETER_NAME);
        assertThat(csrfToken.getToken()).isNotBlank();
    }

    @ParameterizedTest(name = "Tokenvalue {0} returns cookie with value {1}")
    @MethodSource("saveTokenSource")
    void saveToken(String tokenValue, String expectedValue) {
        CsrfToken token = mock(CsrfToken.class);

        when(token.getToken()).thenReturn(tokenValue);
        when(environment.getActiveProfiles()).thenReturn(new String[0]);

        csrfTokenRepository.saveToken(token, request, response);
        assertThat(response.getHeader(SET_COOKIE)).contains(expectedValue);
    }

    private static Stream<Arguments> saveTokenSource() {
        String t = RandomStringUtils.randomAlphabetic(54);
        return Stream.of(
                Arguments.of(null, ""),
                Arguments.of("", ""),
                Arguments.of("", ""),
                Arguments.of(t, t)
        );
    }

    @ParameterizedTest
    @MethodSource("cookieValueSource")
    void cookieValue(Boolean secure, boolean secureCookie, String cookieDomain) {
        String tokenValue = RandomStringUtils.randomAlphabetic(8);
        String cookieName = RandomStringUtils.randomAlphabetic(8);
        String sameSite = RANDOM.nextBoolean() ? SameSiteCookieCsrfTokenRepository.SAME_SITE_LAX : SameSiteCookieCsrfTokenRepository.SAME_SITE_STRICT;
        int maxAge = RANDOM.nextInt() + 1;
        boolean httpOnly = RANDOM.nextBoolean();

        CsrfToken token = mock(CsrfToken.class);

        when(token.getToken()).thenReturn(tokenValue);
        if (Objects.isNull(secure) && !secureCookie) {
            when(environment.getActiveProfiles()).thenReturn(new String[]{"foo", "bar", "local", "test"});
        } else if (Objects.isNull(secure)) {
            when(environment.getActiveProfiles()).thenReturn(new String[]{"foo", "bar", "test"});
        }

        csrfTokenRepository.setCookieName(cookieName);
        csrfTokenRepository.setSameSite(sameSite);
        csrfTokenRepository.setSecure(secure);
        csrfTokenRepository.setCookieMaxAge(maxAge);
        csrfTokenRepository.setCookieHttpOnly(httpOnly);
        csrfTokenRepository.setCookieDomain(cookieDomain);

        csrfTokenRepository.saveToken(token, request, response);

        assertCookieValue(
                response.getHeader(SET_COOKIE),
                Optional.ofNullable(secure).orElse(secureCookie),
                httpOnly,
                maxAge > 0,
                Pair.of(cookieName, tokenValue),
                Pair.of("SameSite", sameSite),
                Pair.of("Path", "/"),
                StringUtils.isNotBlank(cookieDomain) ? Pair.of("Domain", cookieDomain.toLowerCase()) : Pair.of(null, null)
        );
    }

    private static Stream<Arguments> cookieValueSource() {
        return Stream.of(
                Arguments.of(true, false, null),
                Arguments.of(false, true, null),
                Arguments.of(null, true, null),
                Arguments.of(null, false, null),
                Arguments.of(false, false, null),
                Arguments.of(true, true, ""),
                Arguments.of(true, true, RandomStringUtils.randomAlphabetic(10))
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"foo", "bar", " "})
    void loadToken(String token){
        request.setCookies(new Cookie(SameSiteCookieCsrfTokenRepository.DEFAULT_CSRF_COOKIE_NAME, token));

        CsrfToken csrfToken = csrfTokenRepository.loadToken(request);
        if (StringUtils.isEmpty(token)) {
            assertThat(csrfToken).isNull();
        } else {
            assertThat(csrfToken).isNotNull();
            assertThat(csrfToken.getToken()).isEqualTo(token);
            assertThat(csrfToken.getHeaderName()).isEqualTo(SameSiteCookieCsrfTokenRepository.DEFAULT_CSRF_HEADER_NAME);
            assertThat(csrfToken.getParameterName()).isEqualTo(SameSiteCookieCsrfTokenRepository.DEFAULT_CSRF_PARAMETER_NAME);
        }
    }

    @Test
    void nullToken() {
        assertThat(csrfTokenRepository.loadToken(request)).isNull();
    }

    private void assertCookieValue(String cookie, boolean secure, boolean httpOnly, boolean expires ,Pair<Object, Object>... pairs) {
        assertCookie(cookie, SECURE, secure);
        assertCookie(cookie, HTTP_ONLY, httpOnly);
        assertCookie(cookie, EXPIRES, expires);

        Arrays.stream(pairs)
                .filter(p -> p.getLeft() != null && p.getRight() != null)
                .map(p -> "%s=%s".formatted(p.getKey(), p.getValue()))
                .forEach(part -> assertThat(cookie).contains(part));
    }

    private void assertCookie(String cookie, String value, boolean contains) {
        if (contains) {
            assertThat(cookie).contains(value);
        } else {
            assertThat(cookie).doesNotContain(value);
        }
    }
}
