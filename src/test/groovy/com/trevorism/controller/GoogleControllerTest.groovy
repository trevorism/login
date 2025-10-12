package com.trevorism.controller

import com.trevorism.model.Oauth2Tokens
import com.trevorism.service.Oauth2AuthorizationCodeFlow
import org.junit.jupiter.api.Test

class GoogleControllerTest {

    @Test
    void testGetGoogleLoginUrl() {
        GoogleController controller = new GoogleController()
        String authUrl = "http://example.com"
        controller.oauth2AuthorizationCodeFlow = [getAuthorizationUrl: { x,y -> authUrl }] as Oauth2AuthorizationCodeFlow
        assert authUrl == controller.getGoogleLoginUrl(Optional.empty())
        assert authUrl == controller.getGoogleLoginUrl(Optional.of("trevorism.com"))

    }

    @Test
    void testReceiveAuthorizationCodeCallback() {
        GoogleController controller = new GoogleController()
        controller.oauth2AuthorizationCodeFlow = [exchangeCodeForProviderToken: {x,y -> new Oauth2Tokens()},
                                                  getTrevorismToken: {x -> "token"},
                                                  fetchUserInfo: {x -> [:]}
        ] as Oauth2AuthorizationCodeFlow
        assert controller.receiveAuthorizationCodeCallback("code", UUID.randomUUID().toString())
    }
}
