package com.trevorism.service

import org.junit.jupiter.api.Test

class RedirectPolicyTest {

    @Test
    void testPlatformHostsAreAllowed() {
        assert RedirectPolicy.isPlatformReturnUrl("https://trevorism.com")
        assert RedirectPolicy.isPlatformReturnUrl("https://certs.project.trevorism.com/report?tab=1")
        assert RedirectPolicy.isPlatformReturnUrl("https://WWW.Trevorism.com/")
    }

    @Test
    void testEverythingElseIsRejected() {
        assert !RedirectPolicy.isPlatformReturnUrl("https://trevorism.com.evil.net")
        assert !RedirectPolicy.isPlatformReturnUrl("https://eviltrevorism.com")
        assert !RedirectPolicy.isPlatformReturnUrl("http://www.trevorism.com")
        assert !RedirectPolicy.isPlatformReturnUrl("https://user@trevorism.com")
        assert !RedirectPolicy.isPlatformReturnUrl("https://app.memowand.com")
        assert !RedirectPolicy.isPlatformReturnUrl("not a uri")
        assert !RedirectPolicy.isPlatformReturnUrl(null)
        assert !RedirectPolicy.isPlatformReturnUrl("")
    }

    @Test
    void testSafeReturnUrlFallsBackToTheHomePage() {
        assert RedirectPolicy.safeReturnUrl("https://evil.example.org") == RedirectPolicy.DEFAULT_RETURN_URL
        assert RedirectPolicy.safeReturnUrl(null) == RedirectPolicy.DEFAULT_RETURN_URL
        assert RedirectPolicy.safeReturnUrl("https://www.trevorism.com") == "https://www.trevorism.com"
    }

    @Test
    void testCallbackUriForOrigin() {
        assert RedirectPolicy.callbackUriForOrigin("https://app.memowand.com") == "https://app.memowand.com/api/auth/callback"
        assert RedirectPolicy.callbackUriForOrigin("http://localhost:5173") == "http://localhost:5173/api/auth/callback"
        assert RedirectPolicy.callbackUriForOrigin("https://app.memowand.com/anything") == "https://app.memowand.com/api/auth/callback"
        assert RedirectPolicy.callbackUriForOrigin("https://user@app.memowand.com") == null
        assert RedirectPolicy.callbackUriForOrigin("garbage") == null
        assert RedirectPolicy.callbackUriForOrigin(null) == null
    }
}
