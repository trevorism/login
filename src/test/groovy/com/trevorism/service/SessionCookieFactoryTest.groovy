package com.trevorism.service

import io.micronaut.http.cookie.Cookie
import org.junit.jupiter.api.Test

class SessionCookieFactoryTest {

    private static Cookie find(Set<Cookie> cookies, String name) {
        return cookies.find { it.name == name }
    }

    @Test
    void testSessionCookiesAreScopedToThePlatformDomain() {
        Set<Cookie> cookies = SessionCookieFactory.sessionCookies("access", "tester", true, "refresh")

        assert cookies.size() == 4
        cookies.each { assert it.domain == SessionCookieFactory.COOKIE_DOMAIN }
        cookies.each { assert it.path == "/" }
        cookies.each { assert it.secure }
    }

    @Test
    void testTokensAreHttpOnlyAndDisplayValuesAreNot() {
        Set<Cookie> cookies = SessionCookieFactory.sessionCookies("access", "tester", true, "refresh")

        assert find(cookies, SessionCookieFactory.SESSION_COOKIE).httpOnly
        assert find(cookies, SessionCookieFactory.REFRESH_COOKIE).httpOnly
        assert !find(cookies, SessionCookieFactory.USER_NAME_COOKIE).httpOnly
        assert !find(cookies, SessionCookieFactory.ADMIN_COOKIE).httpOnly
        assert find(cookies, SessionCookieFactory.ADMIN_COOKIE).value == "true"
    }

    @Test
    void testRefreshCookieIsOmittedWhenThereIsNoRefreshToken() {
        Set<Cookie> cookies = SessionCookieFactory.sessionCookies("access", "tester", false, null)

        assert cookies.size() == 3
        assert !find(cookies, SessionCookieFactory.REFRESH_COOKIE)
    }

    @Test
    void testDisplayCookiesOutliveTheSessionOnlyWhenItCanBeRefreshed() {
        Set<Cookie> refreshable = SessionCookieFactory.sessionCookies("access", "tester", false, "refresh")
        assert find(refreshable, SessionCookieFactory.USER_NAME_COOKIE).maxAge == SessionCookieFactory.REFRESH_MAX_AGE
        assert find(refreshable, SessionCookieFactory.ADMIN_COOKIE).maxAge == SessionCookieFactory.REFRESH_MAX_AGE
    }

    @Test
    void testDisplayCookiesExpireWithTheSessionWhenThereIsNoRefreshToken() {
        Set<Cookie> oauth = SessionCookieFactory.sessionCookies("access", "tester", false, null)

        assert find(oauth, SessionCookieFactory.SESSION_COOKIE).maxAge == SessionCookieFactory.ACCESS_MAX_AGE
        assert find(oauth, SessionCookieFactory.USER_NAME_COOKIE).maxAge == SessionCookieFactory.ACCESS_MAX_AGE
        assert find(oauth, SessionCookieFactory.ADMIN_COOKIE).maxAge == SessionCookieFactory.ACCESS_MAX_AGE
    }

    @Test
    void testClearedCookiesExpireEverything() {
        Set<Cookie> cookies = SessionCookieFactory.clearedCookies()

        assert cookies.size() == 4
        cookies.each { assert it.maxAge == 0 }
    }
}
