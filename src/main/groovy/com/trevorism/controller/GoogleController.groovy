package com.trevorism.controller

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.trevorism.ClasspathBasedPropertiesProvider
import com.trevorism.http.HttpClient
import com.trevorism.http.JsonHttpClient
import com.trevorism.model.Oauth2Response
import com.trevorism.model.Oauth2Tokens
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.cookie.Cookie
import io.micronaut.http.netty.cookies.NettyCookie
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.NameValuePair
import org.apache.hc.core5.http.message.BasicNameValuePair
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller("/api/google")
class GoogleController {

    private static final Logger log = LoggerFactory.getLogger(GoogleController)
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").create()

    public static String clientId = "20040999009-8gnongpbu2fujg8at7bvl3st1h37hpaq.apps.googleusercontent.com"
    public static final String oauth2AuthCodeUrl = "https://accounts.google.com/o/oauth2/v2/auth"
    public static final String oauth2TokenUrl = "https://oauth2.googleapis.com/token"
    public static final String redirectUri = "https://login.auth.trevorism.com/api/google/callback"
    //public static final String redirectUri = "http://localhost:5173/api/google/callback"

    @Tag(name = "Google Operations")
    @Operation(summary = "Gets a Google login URL")
    @Get(value = "/", produces = MediaType.APPLICATION_JSON)
    String getGoogleLoginUrl( @QueryValue Optional<String> return_url) {
        return getGoogleLoginUrl(null, return_url)
    }

    @Tag(name = "Google Operations")
    @Operation(summary = "Gets a Google login URL for a given tenant")
    @Get(value = "/{guid}", produces = MediaType.APPLICATION_JSON)
    String getGoogleLoginUrl(String guid, @QueryValue Optional<String> return_url) {
        String baseState = UUID.randomUUID().toString()
        String returnUrl = return_url.orElse("https://trevorism.com")
        String stateValue = baseState + "|" + returnUrl
        if(guid){
            stateValue = stateValue + "|" + guid
        }

        String state = URLEncoder.encode(stateValue, "UTF-8")
        return "${oauth2AuthCodeUrl}?client_id=${clientId}&response_type=code&redirect_uri=${redirectUri}&scope=openid%20profile%20email&access_type=online&state=${state}"
    }

    @Tag(name = "Google Operations")
    @Operation(summary = "Receives oauth2 authorization code callback")
    @Get(value = "/callback", produces = MediaType.APPLICATION_JSON)
    HttpResponse receiveAuthorizationCodeCallback(@QueryValue String code, @QueryValue String state) {
        String decodedState = URLDecoder.decode(state, "UTF-8")
        String[] parts = decodedState.split("\\|")
        String returnUrl = parts.length > 1 ? parts[1] : "https://trevorism.com"
        String guid = parts.length > 2 ? parts[2] : null

        String clientSecret = new ClasspathBasedPropertiesProvider().getProperty("apiSecret2")
        CloseableHttpClient httpClient = HttpClients.createDefault()
        HttpPost httpPost = new HttpPost("${oauth2TokenUrl}")
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
        Oauth2Tokens tokens = Oauth2Tokens.fromOauth2Response(oauth2Response, guid)

        HttpClient client = new JsonHttpClient()
        //String token = client.post("http://localhost:8081/google", gson.toJson(tokens))
        String token = client.post("https://auth.trevorism.com/google", gson.toJson(tokens))

        def cookie1 = new NettyCookie("session", token).path("/").maxAge(15 * 60).secure(true).domain(".trevorism.com")
        def cookie2 = new NettyCookie("user_name", "unknown").path("/").maxAge(15 * 60).secure(true).domain(".trevorism.com")
        def cookie3 = new NettyCookie("admin", "false").path("/").maxAge(15 * 60).secure(true).domain(".trevorism.com")

        return HttpResponse.redirect(new URI(returnUrl)).cookies([cookie1, cookie2, cookie3] as Set<Cookie>)
    }
}
