package com.trevorism.service

import com.google.gson.Gson
import com.trevorism.http.HttpClient
import com.trevorism.http.JsonHttpClient
import com.trevorism.https.SecureHttpClient
import com.trevorism.https.SecureHttpClientBase
import com.trevorism.https.token.ObtainTokenFromParameter
import jakarta.inject.Inject
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Singleton
class DefaultHandoffService implements HandoffService {

    private static final Logger log = LoggerFactory.getLogger(DefaultHandoffService)
    static final String HANDOFF_ENDPOINT = "https://auth.trevorism.com/token/handoff"
    static final String ALLOWED_ENDPOINT = "https://auth.trevorism.com/token/handoff/allowed"

    private final Gson gson = new Gson()
    private HttpClient httpClient = new JsonHttpClient()

    @Inject
    @Named("appSecureHttpClient")
    private SecureHttpClient appSecureHttpClient

    @Override
    String mintCode(String accessToken, String refreshToken, String redirectUri) {
        if (!accessToken || !redirectUri) {
            return null
        }
        Map body = [redirectUri: redirectUri]
        if (refreshToken) {
            body.refreshToken = refreshToken
        }
        try {
            String response = callerClient(accessToken).post(HANDOFF_ENDPOINT, gson.toJson(body))
            if (!response || response.trim().startsWith("<")) {
                return null
            }
            return gson.fromJson(response, Map).code
        } catch (Exception e) {
            log.warn("Unable to mint a handoff code for ${redirectUri}: ${e.message}")
            return null
        }
    }

    @Override
    boolean isRedirectAllowed(String redirectUri) {
        if (!redirectUri) {
            return false
        }
        try {
            String response = appSecureHttpClient.get("${ALLOWED_ENDPOINT}?uri=${encode(redirectUri)}")
            return gson.fromJson(response, Map).allowed == true
        } catch (Exception e) {
            log.warn("Unable to check the redirect allowlist for ${redirectUri}: ${e.message}")
            return false
        }
    }

    @Override
    String buildLocation(String redirectUri, String code, String state) {
        String location = "${redirectUri}?code=${encode(code)}"
        if (state) {
            location += "&state=${encode(state)}"
        }
        return location
    }

    private SecureHttpClient callerClient(String accessToken) {
        return new SecureHttpClientBase(httpClient, new ObtainTokenFromParameter(accessToken)) {}
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
    }

    void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient
    }
}
