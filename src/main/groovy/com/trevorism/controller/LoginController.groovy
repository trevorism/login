package com.trevorism.controller

import com.trevorism.model.ForgotPasswordRequest
import com.trevorism.model.LoginRequest
import com.trevorism.model.User
import com.trevorism.service.UserSessionService
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.cookie.Cookie
import io.micronaut.http.netty.cookies.NettyCookie
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import org.apache.hc.client5.http.HttpResponseException

@Controller("/api/login")
class LoginController {

    @Inject
    private UserSessionService userSessionService

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
            throw new HttpResponseException(400, "Invalid username or password")
        }

        User user = userSessionService.getUserFromToken(token)
        if (User.isNullUser(user)) {
            throw new HttpResponseException(400, "Unable to find user")
        }

        def cookie1 = new NettyCookie("session", token).path("/").maxAge(15 * 60).secure(true).domain(".trevorism.com")
        def cookie2 = new NettyCookie("user_name", loginRequest.username).path("/").maxAge(15 * 60).secure(true).domain(".trevorism.com")
        def cookie3 = new NettyCookie("admin", user.admin.toString()).path("/").maxAge(15 * 60).secure(true).domain(".trevorism.com")

        return HttpResponse.ok().cookies([cookie1, cookie2, cookie3] as Set<Cookie>)
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
}
