package com.trevorism.gcloud

import com.trevorism.http.JsonHttpClient
import com.trevorism.http.util.InvalidRequestException

this.metaClass.mixin(io.cucumber.groovy.Hooks)
this.metaClass.mixin(io.cucumber.groovy.EN)

JsonHttpClient anonymousClient = new JsonHttpClient()
String baseUrl = System.getenv("ACCEPTANCE_BASE_URL") ?: "https://login.auth.trevorism.com"

int authorizeStatus
String logoutLocation

When("an authorize request is made for {string} with no cookies") { String redirectUri ->
    authorizeStatus = 0
    try {
        anonymousClient.post("${baseUrl}/api/authorize", """{"redirectUri":"${redirectUri}","state":"acceptance"}""")
        authorizeStatus = 200
    } catch (InvalidRequestException e) {
        authorizeStatus = e.statusCode
    }
}

Then("the authorize request is unauthorized") { ->
    assert authorizeStatus == 401
}

Then("the authorize request is rejected") { ->
    assert authorizeStatus == 400
}

When("a logout is requested with a return to {string}") { String redirectUri ->
    HttpURLConnection connection = new URL("${baseUrl}/api/logout?redirect_uri=${URLEncoder.encode(redirectUri, 'UTF-8')}").openConnection()
    connection.instanceFollowRedirects = false
    connection.requestMethod = "GET"
    logoutLocation = connection.getHeaderField("Location")
    connection.disconnect()
}

Then("the logout sends the browser to {string}") { String expected ->
    assert logoutLocation == expected
}
