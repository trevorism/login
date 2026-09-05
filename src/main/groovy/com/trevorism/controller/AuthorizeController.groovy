package com.trevorism.controller

import com.trevorism.model.AuthorizeRequest
import com.trevorism.model.User
import com.trevorism.service.HandoffService
import com.trevorism.service.SessionCookieFactory
import com.trevorism.service.UserSessionService
import io.micronaut.core.annotation.Nullable
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.CookieValue
import io.micronaut.http.annotation.Post
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import org.apache.hc.client5.http.HttpResponseException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller("/api/authorize")
class AuthorizeController {

    private static final Logger log = LoggerFactory.getLogger(AuthorizeController)

    @Inject
    private UserSessionService userSessionService

    @Inject
    private HandoffService handoffService

    @Tag(name = "Authorize Operations")
    @Operation(summary = "Hands an existing single sign on session to an allowed redirect URI")
    @Post(value = "/", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    HttpResponse authorize(@Body AuthorizeRequest authorizeRequest,
                           @CookieValue("session") @Nullable String sessionToken,
                           @CookieValue("refresh_token") @Nullable String refreshToken) {
        String redirectUri = authorizeRequest?.redirectUri
        if (!handoffService.isRedirectAllowed(redirectUri)) {
            throw new HttpResponseException(400, "This redirect URI is not allowed")
        }

        String accessToken = resolveAccessToken(sessionToken, refreshToken)
        if (!accessToken) {
            return HttpResponse.unauthorized()
        }

        User user = userSessionService.getUserFromToken(accessToken)
        if (User.isNullUser(user)) {
            return HttpResponse.unauthorized()
        }

        String code = handoffService.mintCode(accessToken, refreshToken, redirectUri)
        if (!code) {
            throw new HttpResponseException(400, "Unable to hand off this session")
        }

        log.info("Handing off ${user.username} to ${redirectUri}")
        return HttpResponse.ok([location: handoffService.buildLocation(redirectUri, code, authorizeRequest.state)])
                .cookies(SessionCookieFactory.sessionCookies(accessToken, user.username, user.admin, refreshToken))
    }

    private String resolveAccessToken(String sessionToken, String refreshToken) {
        if (sessionToken && !User.isNullUser(userSessionService.getUserFromToken(sessionToken))) {
            return sessionToken
        }
        return refreshToken ? userSessionService.redeemRefreshToken(refreshToken) : null
    }
}
