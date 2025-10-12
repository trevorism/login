package com.trevorism.controller

import com.trevorism.model.Oauth2Tokens
import com.trevorism.service.Oauth2AuthorizationCodeFlow
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

    @Tag(name = "Google Operations")
    @Operation(summary = "Gets a Google login URL")
    @Get(value = "/", produces = MediaType.APPLICATION_JSON)
    String getGoogleLoginUrl(@QueryValue Optional<String> return_url) {
        return getGoogleLoginUrl(null, return_url)
    }

    @Tag(name = "Google Operations")
    @Operation(summary = "Gets a Google login URL for a given tenant")
    @Get(value = "/{guid}", produces = MediaType.APPLICATION_JSON)
    String getGoogleLoginUrl(String guid, @QueryValue Optional<String> return_url) {
        String returnUrl = return_url.orElse("https://trevorism.com")
        return oauth2AuthorizationCodeFlow.getAuthorizationUrl(guid, returnUrl)
    }

    @Tag(name = "Google Operations")
    @Operation(summary = "Receives oauth2 authorization code callback")
    @Get(value = "/callback", produces = MediaType.APPLICATION_JSON)
    HttpResponse receiveAuthorizationCodeCallback(@QueryValue String code, @QueryValue String state) {
        Oauth2Tokens tokens = oauth2AuthorizationCodeFlow.exchangeCodeForProviderToken(code, state)
        String returnUrl = Oauth2Utils.extractReturnUrlFromState(state)
        return Oauth2Utils.convertCodeIntoHttpResponse(oauth2AuthorizationCodeFlow, tokens, returnUrl)
    }
}
