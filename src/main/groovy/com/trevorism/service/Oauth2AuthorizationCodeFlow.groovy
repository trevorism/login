package com.trevorism.service

import com.trevorism.model.Oauth2Tokens

interface Oauth2AuthorizationCodeFlow {

    String getAuthorizationUrl(String trevorismTenantGuid, String returnUrl)

    Oauth2Tokens exchangeCodeForProviderToken(String code, String state)

    String getTrevorismToken(Oauth2Tokens tokens)

    Map fetchUserInfo(Oauth2Tokens tokens)
}