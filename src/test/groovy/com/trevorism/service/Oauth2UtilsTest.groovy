package com.trevorism.service

import org.junit.jupiter.api.Test

class Oauth2UtilsTest {

    @Test
    void testExtractReturnUrlFromState() {
        String state = "6c306055-cbcf-44e8-8920-38161a5e0332%7Chttps%3A%2F%2Fexample.com%7Ctenant123"
        assert Oauth2Utils.extractReturnUrlFromState(state) == "https://example.com"
    }

    @Test
    void testExtractTenantIdFromState() {
        String state = "6c306055-cbcf-44e8-8920-38161a5e0332%7Chttps%3A%2F%2Fexample.com%7Ctenant123"
        assert Oauth2Utils.extractTenantIdFromState(state) == "tenant123"
    }

    @Test
    void testEncodeState() {
        String state = Oauth2Utils.encodeState("https://example.com", "tenant123")
        println state
        assert state.contains("tenant123")
        assert state.contains("example.com")

    }
}
