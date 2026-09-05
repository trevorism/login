package com.trevorism.service

interface HandoffService {

    String mintCode(String accessToken, String refreshToken, String redirectUri)

    boolean isRedirectAllowed(String redirectUri)

    String buildLocation(String redirectUri, String code, String state)
}
