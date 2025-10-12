package com.trevorism.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.trevorism.model.Oauth2Response
import com.trevorism.model.Oauth2Tokens
import io.micronaut.http.HttpResponse
import io.micronaut.http.cookie.Cookie
import io.micronaut.http.netty.cookies.NettyCookie
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.NameValuePair
import org.apache.hc.core5.http.message.BasicNameValuePair

class Oauth2Utils {

    static final Gson gson = new GsonBuilder().disableHtmlEscaping().setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").create()
    static final CloseableHttpClient httpClient = HttpClients.createDefault()

    static Oauth2Response requestTokenFromOauthProvider(String tokenUrl, String clientId, String clientSecret, String code, String redirectUri) {
        HttpPost httpPost = new HttpPost(tokenUrl)
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded")

        List<NameValuePair> params = [
                new BasicNameValuePair("client_id", clientId),
                new BasicNameValuePair("client_secret", clientSecret),
                new BasicNameValuePair("code", code),
                new BasicNameValuePair("redirect_uri", redirectUri),
                new BasicNameValuePair("grant_type", "authorization_code")
        ]
        httpPost.setEntity(new UrlEncodedFormEntity(params))
        CloseableHttpResponse response = httpClient.execute(httpPost)
        String responseBody = response.entity.content.text

        Oauth2Response oauth2Response = gson.fromJson(responseBody, Oauth2Response)
        return oauth2Response
    }

    static HttpResponse convertCodeIntoHttpResponse(Oauth2AuthorizationCodeFlow oauth2AuthorizationCodeFlow, Oauth2Tokens tokens, String returnUrl) {
        String token = oauth2AuthorizationCodeFlow.getTrevorismToken(tokens)
        Map claims = oauth2AuthorizationCodeFlow.fetchUserInfo(tokens)
        String username = claims["email"] ?: "Unknown"

        def cookie1 = new NettyCookie("session", token).path("/").maxAge(15 * 60).secure(true).domain(".trevorism.com")
        def cookie2 = new NettyCookie("user_name", username).path("/").maxAge(15 * 60).secure(true).domain(".trevorism.com")
        def cookie3 = new NettyCookie("admin", "false").path("/").maxAge(15 * 60).secure(true).domain(".trevorism.com")

        return HttpResponse.redirect(new URI(returnUrl)).cookies([cookie1, cookie2, cookie3] as Set<Cookie>)
    }

    static String extractReturnUrlFromState(String state) {
        String decodedState = URLDecoder.decode(state, "UTF-8")
        String[] parts = decodedState.split("\\|")
        return parts.length > 1 ? parts[1] : "https://trevorism.com"
    }

    static String extractTenantIdFromState(String state) {
        String decodedState = URLDecoder.decode(state, "UTF-8")
        String[] parts = decodedState.split("\\|")
        return parts.length > 2 ? parts[2] : null
    }

    static String encodeState(String returnUrl, String guid) {
        String baseState = UUID.randomUUID().toString()
        String stateValue = baseState + "|" + returnUrl
        if (guid) {
            stateValue = stateValue + "|" + guid
        }

        return URLEncoder.encode(stateValue, "UTF-8")
    }
}
