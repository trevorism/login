package com.trevorism.service

import com.trevorism.https.SecureHttpClient
import com.trevorism.model.Oauth2Tokens
import org.junit.jupiter.api.Test

class MicrosoftAuthorizationCodeFlowTest {

    @Test
    void testGetAuthorizationUrlContainsExpectedParameters() {
        def flow = new MicrosoftAuthorizationCodeFlow()
        String url = flow.getAuthorizationUrl("tenant123", "https://example.com", null, null)

        assert url.startsWith("${MicrosoftAuthorizationCodeFlow.INSTANCE}/${MicrosoftAuthorizationCodeFlow.TENANT_ID}/oauth2/v2.0/authorize")
        assert url.contains("client_id=${MicrosoftAuthorizationCodeFlow.CLIENT_ID}")
        assert url.contains("response_type=code")
        assert url.contains("redirect_uri=${MicrosoftAuthorizationCodeFlow.REDIRECT_URL}")
        assert url.contains("response_mode=query")
        assert Oauth2Utils.decodeState(url.split("state=")[1]).returnUrl == "https://example.com"
        assert Oauth2Utils.decodeState(url.split("state=")[1]).guid == "tenant123"
    }

    @Test
    void testGetTrevorismToken() {
        def flow = new MicrosoftAuthorizationCodeFlow()
        flow.httpClient = [post: { String u, String b -> "jwt-token" }] as SecureHttpClient

        assert flow.getTrevorismToken(new Oauth2Tokens()) == "jwt-token"
    }

    @Test
    void testFetchUserInfoParsesClaims() {
        def flow = new MicrosoftAuthorizationCodeFlow()
        flow.httpClient = [post: { String u, String b -> '{"email":"a@trevorism.com"}' }] as SecureHttpClient

        Map claims = flow.fetchUserInfo(new Oauth2Tokens())
        assert claims["email"] == "a@trevorism.com"
    }
}
