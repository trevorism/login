package com.trevorism.controller

import com.trevorism.service.HandoffService
import com.trevorism.service.RedirectPolicy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LogoutControllerTest {

    private LogoutController controller

    @BeforeEach
    void setup() {
        controller = new LogoutController()
        controller.handoffService = [
                isRedirectAllowed: { String uri -> uri == "https://app.memowand.com/api/auth/callback" }
        ] as HandoffService
    }

    private static String location(response) {
        return response.getHeaders().get("Location")
    }

    @Test
    void testPlatformHostIsHonoured() {
        assert location(controller.logout("https://certs.project.trevorism.com")) == "https://certs.project.trevorism.com"
    }

    @Test
    void testAllowedTenantOriginIsHonoured() {
        assert location(controller.logout("https://app.memowand.com")) == "https://app.memowand.com"
    }

    @Test
    void testUnknownOriginFallsBackToTheHomePage() {
        assert location(controller.logout("https://evil.example.org")) == RedirectPolicy.DEFAULT_RETURN_URL
    }

    @Test
    void testMissingRedirectFallsBackToTheHomePage() {
        assert location(controller.logout(null)) == RedirectPolicy.DEFAULT_RETURN_URL
    }

    @Test
    void testCookiesAreAlwaysCleared() {
        def response = controller.logout("https://evil.example.org")

        assert response.getCookies().getAll().size() == 4
        response.getCookies().getAll().each { assert it.maxAge == 0 }
    }
}
