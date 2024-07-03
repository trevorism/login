package com.trevorism.controller

import com.trevorism.model.ForgotPasswordRequest
import com.trevorism.model.LoginRequest
import com.trevorism.model.User
import com.trevorism.service.UserSessionService
import org.apache.hc.client5.http.HttpResponseException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertThrows

class LoginControllerTest {

    private LoginController loginController

    @BeforeEach
    void setup() {
        loginController = new LoginController()
        loginController.userSessionService = [getToken: { lr, guid -> lr.username },
                                              getUserFromToken: { token -> new User(username: token) },
                                              generateForgotPasswordLink: { val -> if (!val) throw new RuntimeException() else true },
                                              resetPassword: { tId, r -> if (!r) throw new RuntimeException() },
                                              sendLoginEvent: { user -> }] as UserSessionService
    }

    @Test
    void testLogin() {
        def response = loginController.login(new LoginRequest(username: "test", password: "test123"))
        assert response
        assert response.status().getCode() == 200
    }

    @Test
    void testLoginInvalid() {
        assertThrows(HttpResponseException, () -> loginController.login(new LoginRequest(username: null, password: "test123")))
    }

    @Test
    void testForgotPassword() {
        loginController.forgotPassword(new ForgotPasswordRequest(email: "test@trevorism.com"))
        assert true
    }

    @Test
    void testForgotPasswordInvalid() {
        assertThrows(HttpResponseException, () -> loginController.forgotPassword(null))

    }

    @Test
    void testResetPassword() {
        loginController.resetPassword("1234592349")
        assert true
    }

    @Test
    void testResetPasswordInvalid() {
        assertThrows(HttpResponseException, () -> loginController.resetPassword(null))

    }
}
