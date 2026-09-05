package com.trevorism.controller

import com.trevorism.model.AuthorizeRequest
import com.trevorism.model.User
import com.trevorism.service.HandoffService
import com.trevorism.service.SessionCookieFactory
import com.trevorism.service.UserSessionService
import org.apache.hc.client5.http.HttpResponseException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertThrows

class AuthorizeControllerTest {

    private static final String REDIRECT_URI = "https://certs.project.trevorism.com/api/auth/callback"

    private AuthorizeController controller
    private List<String> mintedWith

    @BeforeEach
    void setup() {
        mintedWith = []
        controller = new AuthorizeController()
        controller.userSessionService = [
                getUserFromToken  : { String token -> token == "good.access" ? new User(username: "tester", admin: true) : User.NULL_USER },
                redeemRefreshToken: { String token -> token == "good.refresh" ? "good.access" : null }
        ] as UserSessionService
        controller.handoffService = [
                isRedirectAllowed: { String uri -> uri == REDIRECT_URI },
                mintCode         : { String access, String refresh, String uri -> mintedWith = [access, refresh, uri]; "1000.secret" },
                buildLocation    : { String uri, String code, String state -> "${uri}?code=${code}&state=${state}".toString() }
        ] as HandoffService
    }

    @Test
    void testDisallowedRedirectUriIsRejectedBeforeAnythingElse() {
        assertThrows(HttpResponseException, () -> controller.authorize(
                new AuthorizeRequest(redirectUri: "https://evil.example.org/api/auth/callback"), "good.access", null))
        assert !mintedWith
    }

    @Test
    void testMissingRedirectUriIsRejected() {
        assertThrows(HttpResponseException, () -> controller.authorize(new AuthorizeRequest(), "good.access", null))
    }

    @Test
    void testNoCookiesIsUnauthorized() {
        def response = controller.authorize(new AuthorizeRequest(redirectUri: REDIRECT_URI), null, null)

        assert response.status().code == 401
    }

    @Test
    void testSessionCookieIsUsedWhenItIsStillValid() {
        def response = controller.authorize(new AuthorizeRequest(redirectUri: REDIRECT_URI, state: "abc"), "good.access", "good.refresh")

        assert response.status().code == 200
        assert response.body().location == "${REDIRECT_URI}?code=1000.secret&state=abc"
        assert mintedWith == ["good.access", "good.refresh", REDIRECT_URI]
    }

    @Test
    void testRefreshTokenIsRedeemedWhenTheSessionCookieIsStale() {
        def response = controller.authorize(new AuthorizeRequest(redirectUri: REDIRECT_URI), "expired", "good.refresh")

        assert response.status().code == 200
        assert mintedWith[0] == "good.access"
    }

    @Test
    void testStaleSessionAndUnusableRefreshTokenIsUnauthorized() {
        def response = controller.authorize(new AuthorizeRequest(redirectUri: REDIRECT_URI), "expired", "bad.refresh")

        assert response.status().code == 401
        assert !mintedWith
    }

    @Test
    void testTheSessionIsReissuedAlongsideTheHandoff() {
        def response = controller.authorize(new AuthorizeRequest(redirectUri: REDIRECT_URI), "good.access", "good.refresh")

        def names = response.getCookies().getAll().collect { it.name }
        assert names.contains(SessionCookieFactory.SESSION_COOKIE)
        assert names.contains(SessionCookieFactory.REFRESH_COOKIE)
    }

    @Test
    void testAFailedMintIsRejected() {
        controller.handoffService = [
                isRedirectAllowed: { String uri -> true },
                mintCode         : { String access, String refresh, String uri -> null }
        ] as HandoffService

        assertThrows(HttpResponseException, () -> controller.authorize(
                new AuthorizeRequest(redirectUri: REDIRECT_URI), "good.access", null))
    }
}
