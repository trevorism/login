package com.trevorism.service

import com.trevorism.model.ChangePasswordRequest
import com.trevorism.model.ForgotPasswordRequest
import com.trevorism.model.LoginRequest
import com.trevorism.model.User


interface UserSessionService {

    String getToken(LoginRequest loginRequest)

    User getUserFromToken(String bearerToken)

    boolean generateForgotPasswordLink(ForgotPasswordRequest forgotPasswordRequest)

    void resetPassword(String resetId)

    boolean changePassword(ChangePasswordRequest changePasswordRequest, String bearerToken)
}