package com.trevorism.service


import com.trevorism.PropertiesProvider
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.Oauth2Response
import com.trevorism.model.Oauth2Tokens
import jakarta.inject.Inject
import jakarta.inject.Named

@jakarta.inject.Singleton
@Named("google")
class GoogleAuthorizationCodeFlow implements Oauth2AuthorizationCodeFlow{

    public static final String CLIENT_ID = "20040999009-8gnongpbu2fujg8at7bvl3st1h37hpaq.apps.googleusercontent.com"
    public static final String OAUTH2_AUTH_CODE_URL = "https://accounts.google.com/o/oauth2/v2/auth"
    public static final String OAUTH2_TOKEN_URL = "https://oauth2.googleapis.com/token"
    public static final String REDIRECT_URL = "https://login.auth.trevorism.com/api/google/callback"
    public static final String AUTH_GOOGLE_ENDPOINT = "https://auth.trevorism.com/google"
    //public static final String REDIRECT_URL = "http://localhost:5173/api/google/callback"

    @Inject
    private PropertiesProvider propertiesProvider

    @Inject
    @Named("appSecureHttpClient")
    private SecureHttpClient httpClient

    @Override
    String getAuthorizationUrl(String trevorismTenantGuid, String returnUrl) {
        String state = Oauth2Utils.encodeState(returnUrl, trevorismTenantGuid)
        return "${OAUTH2_AUTH_CODE_URL}?client_id=${CLIENT_ID}&response_type=code&redirect_uri=${REDIRECT_URL}&scope=openid%20profile%20email&access_type=online&state=${state}"
    }

    @Override
    Oauth2Tokens exchangeCodeForProviderToken(String code, String state) {
        String clientSecret = propertiesProvider.getProperty("apiSecret2")
        Oauth2Response oauth2Response = Oauth2Utils.requestTokenFromOauthProvider(OAUTH2_TOKEN_URL, CLIENT_ID, clientSecret, code, REDIRECT_URL)
        String trevorismTenantGuid = Oauth2Utils.extractTenantIdFromState(state)
        Oauth2Tokens tokens = Oauth2Tokens.fromOauth2Response(oauth2Response, trevorismTenantGuid)
        return tokens
    }


    @Override
    String getTrevorismToken(Oauth2Tokens tokens) {
        return httpClient.post(AUTH_GOOGLE_ENDPOINT, Oauth2Utils.gson.toJson(tokens))
    }

    @Override
    Map fetchUserInfo(Oauth2Tokens tokens) {
        String tokenJson = Oauth2Utils.gson.toJson(tokens)
        String responseJson = httpClient.post("$AUTH_GOOGLE_ENDPOINT/claims", tokenJson)
        Map claimsPayload = Oauth2Utils.gson.fromJson(responseJson, Map)
        return claimsPayload
    }
}
