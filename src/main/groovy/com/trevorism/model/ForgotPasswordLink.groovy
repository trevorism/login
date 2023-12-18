package com.trevorism.model

import java.time.Instant
import java.time.temporal.ChronoUnit

class ForgotPasswordLink {

    String id
    String username
    Date expireDate = Date.from(Instant.now().plus(1, ChronoUnit.HOURS))

    String toResetUrl() {
        return "https://www.trevorism.com/api/login/reset/${id}"
    }
}
