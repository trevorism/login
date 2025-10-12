package com.trevorism.model

class Oauth2Tokens {

    String accessToken
    String idToken
    String tokenType
    int expiresIn
    String refreshToken
    String scope
    String tenantId

    static Oauth2Tokens fromOauth2Response(Oauth2Response response, String tenantId) {
        new Oauth2Tokens(
                accessToken: response.access_token,
                idToken: response.id_token,
                tokenType: response.token_type,
                expiresIn: response.expires_in,
                refreshToken: response.refresh_token,
                scope: response.scope,
                tenantId: tenantId
        )
    }
}
