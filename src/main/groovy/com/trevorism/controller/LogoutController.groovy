package com.trevorism.controller

import com.trevorism.service.HandoffService
import com.trevorism.service.RedirectPolicy
import com.trevorism.service.SessionCookieFactory
import io.micronaut.core.annotation.Nullable
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller("/api/logout")
class LogoutController {

    private static final Logger log = LoggerFactory.getLogger(LogoutController)

    @Inject
    private HandoffService handoffService

    @Tag(name = "Logout Operations")
    @Operation(summary = "Clears the platform session and returns to an allowed URI")
    @Get(value = "/")
    HttpResponse logout(@QueryValue("redirect_uri") @Nullable String redirectUri) {
        String target = resolveTarget(redirectUri)
        log.info("Clearing the platform session, returning to ${target}")
        return HttpResponse.redirect(new URI(target)).cookies(SessionCookieFactory.clearedCookies())
    }

    private String resolveTarget(String redirectUri) {
        if (!redirectUri) {
            return RedirectPolicy.DEFAULT_RETURN_URL
        }
        if (RedirectPolicy.isPlatformReturnUrl(redirectUri)) {
            return redirectUri
        }
        String callback = RedirectPolicy.callbackUriForOrigin(redirectUri)
        if (callback && handoffService.isRedirectAllowed(callback)) {
            return redirectUri
        }
        return RedirectPolicy.DEFAULT_RETURN_URL
    }
}
