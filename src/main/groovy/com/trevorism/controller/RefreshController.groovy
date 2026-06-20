package com.trevorism.controller

import com.trevorism.model.User
import com.trevorism.service.UserSessionService
import io.micronaut.core.annotation.Nullable
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.CookieValue
import io.micronaut.http.annotation.Post
import io.micronaut.http.cookie.Cookie
import io.micronaut.http.netty.cookies.NettyCookie
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller("/api/refresh")
class RefreshController {

    private static final Logger log = LoggerFactory.getLogger(RefreshController)

    @Inject
    private UserSessionService userSessionService

    @Tag(name = "Refresh Operations")
    @Operation(summary = "Exchange the refresh token cookie for a fresh session token")
    @Post(value = "/", produces = MediaType.APPLICATION_JSON)
    HttpResponse refresh(@CookieValue("refresh_token") @Nullable String refreshToken) {
        if (!refreshToken) {
            return HttpResponse.unauthorized()
        }

        String token = userSessionService.redeemRefreshToken(refreshToken)
        if (!token) {
            return HttpResponse.unauthorized().cookies(clearedCookies())
        }

        User user = userSessionService.getUserFromToken(token)
        if (User.isNullUser(user)) {
            return HttpResponse.unauthorized().cookies(clearedCookies())
        }

        int accessMaxAge = 15 * 60
        int refreshMaxAge = 24 * 60 * 60

        def sessionCookie = new NettyCookie("session", token).path("/").maxAge(accessMaxAge).secure(true).domain(".trevorism.com").httpOnly(true)
        def userCookie = new NettyCookie("user_name", user.username).path("/").maxAge(refreshMaxAge).secure(true).domain(".trevorism.com")
        def adminCookie = new NettyCookie("admin", user.admin.toString()).path("/").maxAge(refreshMaxAge).secure(true).domain(".trevorism.com")

        return HttpResponse.ok([status: "refreshed"]).cookies([sessionCookie, userCookie, adminCookie] as Set<Cookie>)
    }

    private static Set<Cookie> clearedCookies() {
        def session = new NettyCookie("session", "").path("/").maxAge(0).secure(true).domain(".trevorism.com").httpOnly(true)
        def userName = new NettyCookie("user_name", "").path("/").maxAge(0).secure(true).domain(".trevorism.com")
        def admin = new NettyCookie("admin", "").path("/").maxAge(0).secure(true).domain(".trevorism.com")
        def refresh = new NettyCookie("refresh_token", "").path("/").maxAge(0).secure(true).domain(".trevorism.com").httpOnly(true)
        return [session, userName, admin, refresh] as Set<Cookie>
    }
}
