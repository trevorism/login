package com.trevorism.service

import org.junit.jupiter.api.Test

class Oauth2UtilsTest {

    @Test
    void testLegacyPipeStateStillDecodes() {
        String state = "6c306055-cbcf-44e8-8920-38161a5e0332%7Chttps%3A%2F%2Fexample.com%7Ctenant123"
        assert Oauth2Utils.extractReturnUrlFromState(state) == "https://example.com"
        assert Oauth2Utils.extractTenantIdFromState(state) == "tenant123"
    }

    @Test
    void testStateRoundTripsTheHandoffDetails() {
        String state = Oauth2Utils.encodeState("https://www.trevorism.com", "tenant123",
                "https://certs.project.trevorism.com/api/auth/callback", "nonce-from-the-app")

        def decoded = Oauth2Utils.decodeState(state)

        assert decoded.returnUrl == "https://www.trevorism.com"
        assert decoded.guid == "tenant123"
        assert decoded.redirectUri == "https://certs.project.trevorism.com/api/auth/callback"
        assert decoded.state == "nonce-from-the-app"
        assert decoded.nonce
    }

    @Test
    void testStateWithoutHandoffDetails() {
        String state = Oauth2Utils.encodeState("https://www.trevorism.com", "tenant123")

        def decoded = Oauth2Utils.decodeState(state)

        assert decoded.returnUrl == "https://www.trevorism.com"
        assert decoded.guid == "tenant123"
        assert !decoded.redirectUri
    }

    @Test
    void testGarbageStateDoesNotThrow() {
        assert Oauth2Utils.extractReturnUrlFromState("") == "https://trevorism.com"
        assert !Oauth2Utils.extractTenantIdFromState("nonsense")
    }
}
