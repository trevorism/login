package com.trevorism.controller

import com.trevorism.http.async.AsyncHttpClient
import com.trevorism.http.async.AsyncJsonHttpClient
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.runtime.server.event.ServerStartupEvent
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.apache.hc.core5.concurrent.FutureCallback
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller("/api")
class RootController implements ApplicationEventListener<ServerStartupEvent> {

    private static final Logger log = LoggerFactory.getLogger(RootController)
    String host

    @Tag(name = "Root Operations")
    @Operation(summary = "Context Root of the Application")
    @ApiResponse(
            responseCode = "200", content = @Content(mediaType = "text/html", schema = @Schema(type = "string"))
    )

    @Tag(name = "Root Operations")
    @Get(produces = MediaType.TEXT_HTML)
    HttpResponse<String> index() {
        log.info("Hit context root")
        HttpResponse.ok(['<a href="/api/ping">/ping</a>', '<a href="/api/help">/help</a>', '<a href="/api/version">/version</a>'].toString())
    }

    @Tag(name = "Root Operations")
    @Operation(summary = "Returns 'pong' on success")
    @ApiResponse(
            responseCode = "200", content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))
    )
    @Get(value = "/ping", produces = MediaType.TEXT_PLAIN)
    String ping() {
        return "pong"
    }

    @Tag(name = "Root Operations")
    @Operation(summary = "This help page")
    @ApiResponse(responseCode = "302")
    @Get(value = "/help")
    HttpResponse<String> help() {
        return HttpResponse.redirect(new URI("../swagger-ui/index.html"))
    }

    @Tag(name = "Root Operations")
    @Operation(summary = "Returns the version of the API")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "text/plain",
                    schema = @Schema(type = "string"))
    )
    @Get(value = "/version", produces = MediaType.TEXT_PLAIN)
    String version() {
        return "0.0.1"
    }

    @Tag(name = "Root Operations")
    @Operation(summary = "Warms up the authorization service")
    @Get(value = "/authWarmup")
    void warmupAuthService(){
        AsyncHttpClient client = new AsyncJsonHttpClient()
        client.get("https://datastore.trevorism.com/ping", {} as FutureCallback)
        client.get("https://auth.trevorism.com/ping", {} as FutureCallback)
    }

    @Tag(name = "Root Operations")
    @Operation(summary = "Gets the current host")
    @Get(value = "/host")
    String getCurrentHost(){
        return host
    }

    @Override
    void onApplicationEvent(ServerStartupEvent event) {
        host = event.getSource().getHost()
    }
}
