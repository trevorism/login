package com.trevorism.service

import com.trevorism.https.SecureHttpClient
import com.trevorism.model.Oauth2Tokens
import org.junit.jupiter.api.Test

class GoogleAuthorizationCodeFlowTest {

    @Test
    void testGetAuthorizationUrlContainsExpectedParameters() {
        def flow = new GoogleAuthorizationCodeFlow()
        String url = flow.getAuthorizationUrl("tenant123", "https://example.com")

        assert url.startsWith(GoogleAuthorizationCodeFlow.OAUTH2_AUTH_CODE_URL)
        assert url.contains("client_id=${GoogleAuthorizationCodeFlow.CLIENT_ID}")
        assert url.contains("response_type=code")
        assert url.contains("redirect_uri=${GoogleAuthorizationCodeFlow.REDIRECT_URL}")
        assert url.contains("scope=openid%20profile%20email")
        assert url.contains("example.com")
        assert url.contains("tenant123")
    }

    @Test
    void testGetAuthorizationUrlWithoutGuid() {
        def flow = new GoogleAuthorizationCodeFlow()
        String url = flow.getAuthorizationUrl(null, "https://example.com")

        assert url.contains("state=")
        assert url.contains("example.com")
    }

    @Test
    void testGetTrevorismToken() {
        def flow = new GoogleAuthorizationCodeFlow()
        flow.httpClient = [post: { String u, String b -> "jwt-token" }] as SecureHttpClient

        assert flow.getTrevorismToken(new Oauth2Tokens()) == "jwt-token"
    }

    @Test
    void testFetchUserInfoParsesClaims() {
        def flow = new GoogleAuthorizationCodeFlow()
        flow.httpClient = [post: { String u, String b -> '{"email":"a@trevorism.com","name":"A"}' }] as SecureHttpClient

        Map claims = flow.fetchUserInfo(new Oauth2Tokens())
        assert claims["email"] == "a@trevorism.com"
        assert claims["name"] == "A"
    }
}
