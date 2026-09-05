package com.trevorism.controller

import com.trevorism.model.Oauth2Tokens
import com.trevorism.model.Oauth2State
import com.trevorism.service.HandoffService
import com.trevorism.service.Oauth2AuthorizationCodeFlow
import com.trevorism.service.RedirectPolicy
import com.trevorism.service.Oauth2Utils
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import jakarta.inject.Named
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller("/api/google")
class GoogleController {

    private static final Logger log = LoggerFactory.getLogger(GoogleController)

    @Inject
    @Named("google")
    private Oauth2AuthorizationCodeFlow oauth2AuthorizationCodeFlow

    @Inject
    private HandoffService handoffService

    @Tag(name = "Google Operations")
    @Operation(summary = "Gets a Google login URL")
    @Get(value = "/", produces = MediaType.APPLICATION_JSON)
    String getGoogleLoginUrl(@QueryValue Optional<String> return_url, @QueryValue Optional<String> redirect_uri,
                              @QueryValue Optional<String> state) {
        return getGoogleLoginUrl(null, return_url, redirect_uri, state)
    }

    @Tag(name = "Google Operations")
    @Operation(summary = "Gets a Google login URL for a given tenant")
    @Get(value = "/{guid}", produces = MediaType.APPLICATION_JSON)
    String getGoogleLoginUrl(String guid, @QueryValue Optional<String> return_url, @QueryValue Optional<String> redirect_uri,
                              @QueryValue Optional<String> state) {
        String returnUrl = RedirectPolicy.safeReturnUrl(return_url.orElse(RedirectPolicy.DEFAULT_RETURN_URL))
        return oauth2AuthorizationCodeFlow.getAuthorizationUrl(guid, returnUrl, redirect_uri.orElse(null), state.orElse(null))
    }

    @Tag(name = "Google Operations")
    @Operation(summary = "Receives oauth2 authorization code callback")
    @Get(value = "/callback", produces = MediaType.APPLICATION_JSON)
    HttpResponse receiveAuthorizationCodeCallback(@QueryValue String code, @QueryValue String state) {
        Oauth2Tokens tokens = oauth2AuthorizationCodeFlow.exchangeCodeForProviderToken(code, state)
        Oauth2State oauth2State = Oauth2Utils.decodeState(state)
        return Oauth2Utils.convertCodeIntoHttpResponse(oauth2AuthorizationCodeFlow, tokens, oauth2State, handoffService)
    }
}
