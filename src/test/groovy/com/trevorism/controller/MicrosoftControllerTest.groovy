package com.trevorism.controller

import com.trevorism.model.Oauth2Tokens
import com.trevorism.service.Oauth2AuthorizationCodeFlow
import org.junit.jupiter.api.Test

class MicrosoftControllerTest {

    @Test
    void testGetGoogleLoginUrl() {
        MicrosoftController controller = new MicrosoftController()
        String authUrl = "http://example.com"
        controller.oauth2AuthorizationCodeFlow = [getAuthorizationUrl: { x,y -> authUrl }] as Oauth2AuthorizationCodeFlow
        assert authUrl == controller.getMicrosoftLoginUrl(Optional.empty())
        assert authUrl == controller.getMicrosoftLoginUrl(Optional.of("trevorism.com"))

    }

    @Test
    void testReceiveAuthorizationCodeCallback() {
        MicrosoftController controller = new MicrosoftController()
        controller.oauth2AuthorizationCodeFlow = [exchangeCodeForProviderToken: {x,y -> new Oauth2Tokens()},
                                                  getTrevorismToken: {x -> "token"},
                                                  fetchUserInfo: {x -> [:]}
        ] as Oauth2AuthorizationCodeFlow
        assert controller.receiveAuthorizationCodeCallback("code", UUID.randomUUID().toString())
    }
}
