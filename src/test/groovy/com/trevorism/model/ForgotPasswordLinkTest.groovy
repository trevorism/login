package com.trevorism.model

import org.junit.jupiter.api.Test

class ForgotPasswordLinkTest {

    @Test
    void testToResetUrl() {
        ForgotPasswordLink forgotPasswordLink = new ForgotPasswordLink(id: "test")
        assert "https://login.auth.trevorism.com/api/login/reset/test" == forgotPasswordLink.toResetUrl()
    }
}
