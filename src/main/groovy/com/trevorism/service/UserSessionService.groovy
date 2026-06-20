package com.trevorism.service


import com.trevorism.model.ForgotPasswordRequest
import com.trevorism.model.LoginRequest
import com.trevorism.model.User


interface UserSessionService {

    String getToken(LoginRequest loginRequest, String guid)

    String getRefreshToken(LoginRequest loginRequest, String guid)

    String redeemRefreshToken(String refreshToken)

    User getUserFromToken(String bearerToken)

    boolean generateForgotPasswordLink(ForgotPasswordRequest forgotPasswordRequest)

    void resetPassword(String tenantId, String resetId)

}