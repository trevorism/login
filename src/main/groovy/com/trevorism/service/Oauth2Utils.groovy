package com.trevorism.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.trevorism.model.Oauth2Response
import com.trevorism.model.Oauth2State
import com.trevorism.model.Oauth2Tokens
import io.micronaut.http.HttpResponse
import io.micronaut.http.cookie.Cookie
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

    static HttpResponse convertCodeIntoHttpResponse(Oauth2AuthorizationCodeFlow oauth2AuthorizationCodeFlow, Oauth2Tokens tokens,
                                                    Oauth2State oauth2State, HandoffService handoffService) {
        String token = oauth2AuthorizationCodeFlow.getTrevorismToken(tokens)
        Map claims = oauth2AuthorizationCodeFlow.fetchUserInfo(tokens)
        String username = claims["email"] ?: "Unknown"
        Set<Cookie> cookies = SessionCookieFactory.sessionCookies(token, username, false, null)

        String location = handoffLocation(handoffService, token, oauth2State)
        if (!location) {
            location = RedirectPolicy.safeReturnUrl(oauth2State?.returnUrl)
        }
        return HttpResponse.redirect(new URI(location)).cookies(cookies)
    }

    private static String handoffLocation(HandoffService handoffService, String token, Oauth2State oauth2State) {
        if (!oauth2State?.redirectUri || !handoffService) {
            return null
        }
        if (!handoffService.isRedirectAllowed(oauth2State.redirectUri)) {
            return null
        }
        String code = handoffService.mintCode(token, null, oauth2State.redirectUri)
        return code ? handoffService.buildLocation(oauth2State.redirectUri, code, oauth2State.state) : null
    }

    static String extractReturnUrlFromState(String state) {
        return decodeState(state).returnUrl ?: "https://trevorism.com"
    }

    static String extractTenantIdFromState(String state) {
        return decodeState(state).guid
    }

    static Oauth2State decodeState(String state) {
        String decodedState = URLDecoder.decode(state ?: "", "UTF-8")
        try {
            String json = new String(Base64.urlDecoder.decode(decodedState), "UTF-8")
            Oauth2State parsed = gson.fromJson(json, Oauth2State)
            if (parsed?.nonce) {
                return parsed
            }
        } catch (Exception ignored) {
        }
        return legacyState(decodedState)
    }

    static String encodeState(String returnUrl, String guid) {
        return encodeState(returnUrl, guid, null, null)
    }

    static String encodeState(String returnUrl, String guid, String redirectUri, String appState) {
        Oauth2State oauth2State = new Oauth2State(
                nonce: UUID.randomUUID().toString(),
                returnUrl: returnUrl,
                guid: guid,
                redirectUri: redirectUri,
                state: appState)
        String encoded = Base64.urlEncoder.withoutPadding().encodeToString(gson.toJson(oauth2State).getBytes("UTF-8"))
        return URLEncoder.encode(encoded, "UTF-8")
    }

    private static Oauth2State legacyState(String decodedState) {
        String[] parts = decodedState.split(java.util.regex.Pattern.quote("|"))
        return new Oauth2State(
                nonce: parts.length > 0 ? parts[0] : null,
                returnUrl: parts.length > 1 ? parts[1] : null,
                guid: parts.length > 2 ? parts[2] : null)
    }
}
