package com.trevorism.controller

import com.trevorism.http.async.AsyncHttpClient
import com.trevorism.http.async.AsyncJsonHttpClient
import com.trevorism.model.ForgotPasswordRequest
import com.trevorism.model.LoginEvent
import com.trevorism.model.LoginRequest
import com.trevorism.model.User
import com.trevorism.service.HandoffService
import com.trevorism.service.SessionCookieFactory
import com.trevorism.service.UserSessionService
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.cookie.Cookie
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import org.apache.hc.client5.http.HttpResponseException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller("/api/login")
class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController)

    @Inject
    private UserSessionService userSessionService

    @Inject
    private HandoffService handoffService

    private AsyncHttpClient asyncHttpClient = new AsyncJsonHttpClient()

    @Tag(name = "Login Operations")
    @Operation(summary = "Login to Trevorism")
    @Post(value = "/", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    HttpResponse login(@Body LoginRequest loginRequest) {
        return login(loginRequest, null)
    }

    @Tag(name = "Login Operations")
    @Operation(summary = "Login to Trevorism with the given tenant")
    @Post(value = "/{guid}", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    HttpResponse login(@Body LoginRequest loginRequest, String guid) {
        String token = userSessionService.getToken(loginRequest, guid)
        if (!token) {
            sendLoginEvent(loginRequest, guid, false)
            throw new HttpResponseException(400, "Invalid username or password")
        }

        User user = userSessionService.getUserFromToken(token)
        if (User.isNullUser(user)) {
            sendLoginEvent(loginRequest, guid, false)
            throw new HttpResponseException(400, "Unable to find user")
        }

        String refreshToken = userSessionService.getRefreshToken(loginRequest, guid)
        Set<Cookie> cookies = SessionCookieFactory.sessionCookies(token, loginRequest.username, user.admin, refreshToken)

        sendLoginEvent(loginRequest, guid, true)

        if (!loginRequest.redirectUri) {
            return HttpResponse.ok().cookies(cookies)
        }
        if (!handoffService.isRedirectAllowed(loginRequest.redirectUri)) {
            return HttpResponse.badRequest([message: "This redirect URI is not allowed"]).cookies(cookies)
        }
        String code = handoffService.mintCode(token, refreshToken, loginRequest.redirectUri)
        if (!code) {
            return HttpResponse.badRequest([message: "Unable to hand off this session"]).cookies(cookies)
        }
        String location = handoffService.buildLocation(loginRequest.redirectUri, code, loginRequest.state)
        return HttpResponse.ok([location: location]).cookies(cookies)
    }

    @Tag(name = "Login Operations")
    @Operation(summary = "Sends an email for forgotten passwords")
    @Post(value = "/forgot", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    boolean forgotPassword(@Body ForgotPasswordRequest request) {
        try {
            return userSessionService.generateForgotPasswordLink(request)
        } catch (Exception e) {
            throw new HttpResponseException(400, e.message)
        }
    }

    @Tag(name = "Login Operations")
    @Operation(summary = "Resets password")
    @Get(value = "/reset/{resetId}", produces = MediaType.APPLICATION_JSON)
    String resetPassword(String resetId) {
        return resetPassword(null, resetId)
    }

    @Tag(name = "Login Operations")
    @Operation(summary = "Resets password for a given tenant")
    @Get(value = "/reset/{tenantId}/{resetId}", produces = MediaType.APPLICATION_JSON)
    String resetPassword(String tenantId, String resetId) {
        try {
            userSessionService.resetPassword(tenantId, resetId)
            return "Password reset successfully. Check your email for your temporary password."
        } catch (Exception e) {
            throw new HttpResponseException(400, e.message)
        }
    }

    private void sendLoginEvent(LoginRequest loginRequest, String guid, boolean success) {
        String eventJson = LoginEvent.createEventJson(loginRequest.username, guid, success)
        asyncHttpClient.post("https://event.data.trevorism.com/event/login", eventJson, null)
    }

}
