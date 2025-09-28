package com.trevorism.controller

import com.trevorism.ClasspathBasedPropertiesProvider
import com.trevorism.http.HttpClient
import com.trevorism.http.JsonHttpClient
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

@Controller("/api/microsoft")
class MicrosoftController {

    private static final Logger log = LoggerFactory.getLogger(MicrosoftController)

    public static final String tenantId = "d77da90e-329a-41c3-b8b7-f76b8bf71b06"
    public static final String clientId = "c3ede79b-cc30-4f21-818c-45f727113b0e"
    public static final String instance = "https://login.microsoftonline.com"
    public static final String redirectUri = "https://login.auth.trevorism.com/api/microsoft/callback"
    //public static final String redirectUri = "http://localhost:5173/api/microsoft/callback"

    @Tag(name = "Microsoft Operations")
    @Operation(summary = "Gets a Microsoft login URL")
    @Get(value = "/", produces = MediaType.APPLICATION_JSON)
    String getMicrosoftLoginUrl( @QueryValue Optional<String> return_url) {
        return getMicrosoftLoginUrl(null, return_url)
    }

    @Tag(name = "Microsoft Operations")
    @Operation(summary = "Gets a Microsoft login URL for a given tenant")
    @Get(value = "/{guid}", produces = MediaType.APPLICATION_JSON)
    String getMicrosoftLoginUrl(String guid, @QueryValue Optional<String> return_url) {
        String baseState = UUID.randomUUID().toString()
        String returnUrl = return_url.orElse("https://trevorism.com")
        String stateValue = baseState + "|" + returnUrl
        if(guid){
            stateValue = stateValue + "|" + guid
        }

        String state = URLEncoder.encode(stateValue, "UTF-8")

        return "${instance}/${tenantId}/oauth2/v2.0/authorize?client_id=${clientId}&response_type=code&redirect_uri=${redirectUri}&response_mode=query&scope=openid%20profile%20email&state=${state}"
    }

    @Tag(name = "Microsoft Operations")
    @Operation(summary = "Resets password for a given tenant")
    @Get(value = "/callback", produces = MediaType.APPLICATION_JSON)
    HttpResponse receiveAuthorizationCodeCallback(@QueryValue String code, @QueryValue String state) {
        String decodedState = URLDecoder.decode(state, "UTF-8")
        String[] parts = decodedState.split("\\|")
        String returnUrl = parts.length > 1 ? parts[1] : "https://trevorism.com"
        String guid = parts.length > 2 ? parts[2] : null

        String clientSecret = new ClasspathBasedPropertiesProvider().getProperty("apiSecret")
        CloseableHttpClient httpClient = HttpClients.createDefault()
        HttpPost httpPost = new HttpPost("${instance}/${tenantId}/oauth2/token")
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

        //TODO: append guid as tenantId to responseBody
        HttpClient client = new JsonHttpClient()
        String token = client.post("https://auth.trevorism.com/microsoft", responseBody)

        def cookie1 = new NettyCookie("session", token).path("/").maxAge(15 * 60).secure(true).domain(".trevorism.com")
        def cookie2 = new NettyCookie("user_name", "unknown").path("/").maxAge(15 * 60).secure(true).domain(".trevorism.com")
        def cookie3 = new NettyCookie("admin", "false").path("/").maxAge(15 * 60).secure(true).domain(".trevorism.com")

        return HttpResponse.redirect(new URI(returnUrl)).cookies([cookie1, cookie2, cookie3] as Set<Cookie>)
    }
}
