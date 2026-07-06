package com.trevorism.service

import com.trevorism.PropertiesProvider
import com.trevorism.http.HttpClient
import com.trevorism.model.LoginRequest
import com.trevorism.model.User
import org.junit.jupiter.api.Test

class DefaultUserSessionServiceTest {

    @Test
    void testGetTokenSuccess() {
        def service = buildService([post: { String u, String b -> "header.payload.sig" }] as HttpClient)
        assert service.getToken(login(), "guid") == "header.payload.sig"
    }

    @Test
    void testGetTokenReturnsNullOnHtmlResponse() {
        def service = buildService([post: { String u, String b -> "<html>Bad Request</html>" }] as HttpClient)
        assert service.getToken(login(), "guid") == null
    }

    @Test
    void testGetTokenReturnsNullOnException() {
        def service = buildService([post: { String u, String b -> throw new RuntimeException("boom") }] as HttpClient)
        assert service.getToken(login(), "guid") == null
    }

    @Test
    void testGetRefreshTokenSuccess() {
        def service = buildService([post: { String u, String b -> "refresh-token" }] as HttpClient)
        assert service.getRefreshToken(login(), "guid") == "refresh-token"
    }

    @Test
    void testGetRefreshTokenReturnsNullOnHtmlResponse() {
        def service = buildService([post: { String u, String b -> "<html>nope" }] as HttpClient)
        assert service.getRefreshToken(login(), "guid") == null
    }

    @Test
    void testRedeemRefreshTokenNullGuardDoesNotCallNetwork() {
        def service = buildService([post: { String u, String b -> throw new IllegalStateException("network call!") }] as HttpClient)
        assert service.redeemRefreshToken(null) == null
        assert service.redeemRefreshToken("") == null
    }

    @Test
    void testRedeemRefreshTokenSuccess() {
        def service = buildService([post: { String u, String b -> "new-access-token" }] as HttpClient)
        assert service.redeemRefreshToken("some-refresh") == "new-access-token"
    }

    @Test
    void testRedeemRefreshTokenReturnsNullOnHtmlResponse() {
        def service = buildService([post: { String u, String b -> "<html>bad" }] as HttpClient)
        assert service.redeemRefreshToken("some-refresh") == null
    }

    @Test
    void testGetUserFromTokenReturnsNullUserOnInvalidToken() {
        def props = [getProperty: { String key -> "fake-signing-key" }] as PropertiesProvider
        def service = buildService([post: { String u, String b -> "" }] as HttpClient, props)

        User user = service.getUserFromToken("not-a-valid-jwt")
        assert user.is(User.NULL_USER)
    }

    private static DefaultUserSessionService buildService(HttpClient fakeClient, PropertiesProvider props = null) {
        DefaultUserSessionService service = new DefaultUserSessionService()
        service.singletonClient = fakeClient
        if (props) {
            service.propertiesProvider = props
        }
        return service
    }

    private static LoginRequest login() {
        new LoginRequest(username: "user", password: "pass")
    }
}
