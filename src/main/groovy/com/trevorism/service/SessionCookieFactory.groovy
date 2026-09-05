package com.trevorism.service

import io.micronaut.http.cookie.Cookie
import io.micronaut.http.netty.cookies.NettyCookie

class SessionCookieFactory {

    static final String SESSION_COOKIE = "session"
    static final String REFRESH_COOKIE = "refresh_token"
    static final String USER_NAME_COOKIE = "user_name"
    static final String ADMIN_COOKIE = "admin"
    static final String COOKIE_DOMAIN = ".trevorism.com"

    static final int ACCESS_MAX_AGE = 15 * 60
    static final int REFRESH_MAX_AGE = 24 * 60 * 60

    static Set<Cookie> sessionCookies(String accessToken, String username, boolean admin, String refreshToken) {
        Set<Cookie> cookies = new LinkedHashSet<>()
        cookies.add(cookie(SESSION_COOKIE, accessToken, ACCESS_MAX_AGE, true))
        cookies.add(cookie(USER_NAME_COOKIE, username ?: "", REFRESH_MAX_AGE, false))
        cookies.add(cookie(ADMIN_COOKIE, Boolean.toString(admin), REFRESH_MAX_AGE, false))
        if (refreshToken) {
            cookies.add(cookie(REFRESH_COOKIE, refreshToken, REFRESH_MAX_AGE, true))
        }
        return cookies
    }

    static Set<Cookie> clearedCookies() {
        Set<Cookie> cookies = new LinkedHashSet<>()
        cookies.add(cookie(SESSION_COOKIE, "", 0, true))
        cookies.add(cookie(USER_NAME_COOKIE, "", 0, false))
        cookies.add(cookie(ADMIN_COOKIE, "", 0, false))
        cookies.add(cookie(REFRESH_COOKIE, "", 0, true))
        return cookies
    }

    private static Cookie cookie(String name, String value, int maxAge, boolean httpOnly) {
        return new NettyCookie(name, value)
                .path("/")
                .maxAge(maxAge)
                .secure(true)
                .domain(COOKIE_DOMAIN)
                .httpOnly(httpOnly)
    }
}
