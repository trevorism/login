package com.trevorism.service

import com.google.gson.Gson
import com.trevorism.ClaimProperties
import com.trevorism.ClaimsProvider
import com.trevorism.PropertiesProvider
import com.trevorism.data.FastDatastoreRepository
import com.trevorism.data.Repository
import com.trevorism.http.HttpClient
import com.trevorism.http.JsonHttpClient
import com.trevorism.https.SecureHttpClient
import com.trevorism.https.SecureHttpClientBase
import com.trevorism.https.token.ObtainTokenFromAuthServiceFromPropertiesFile
import com.trevorism.https.token.ObtainTokenFromParameter
import com.trevorism.model.Email
import com.trevorism.model.ForgotPasswordLink
import com.trevorism.model.ForgotPasswordRequest
import com.trevorism.model.InternalTokenRequest
import com.trevorism.model.LoginRequest
import com.trevorism.model.TokenRequest
import com.trevorism.model.User
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@jakarta.inject.Singleton
class DefaultUserSessionService implements UserSessionService {

    private static final Logger log = LoggerFactory.getLogger(DefaultUserSessionService.class.getName())

    private HttpClient singletonClient = new JsonHttpClient()
    @Inject
    private PropertiesProvider propertiesProvider
    private final Gson gson = new Gson()

    @Override
    String getToken(LoginRequest loginRequest, String guid) {
        String json = gson.toJson(TokenRequest.fromLoginRequest(loginRequest, guid))
        try {
            return invokeTokenRequest(json)
        } catch (Exception e) {
            log.debug("Invalid login", e)
        }
        return null
    }

    @Override
    User getUserFromToken(String token) {
        try {
            ClaimProperties claimProperties = ClaimsProvider.getClaims(token, propertiesProvider.getProperty("signingKey"))
            SecureHttpClient internalHttpClient = new SecureHttpClientBase(singletonClient, new ObtainTokenFromParameter(token)) {}
            def response = internalHttpClient.get("https://auth.trevorism.com/user/${claimProperties.id}")
            User user = gson.fromJson(response, User)
            return user
        } catch (Exception e) {
            log.warn("Unable to find user", e)
            return User.NULL_USER
        }
    }


    @Override
    boolean generateForgotPasswordLink(ForgotPasswordRequest forgotPasswordRequest) {
        String token = getInternalToken(forgotPasswordRequest.tenantId)
        SecureHttpClient internalHttpClient = new SecureHttpClientBase(singletonClient, new ObtainTokenFromParameter(token)) {}
        Repository<User> repository = new FastDatastoreRepository<>(User, internalHttpClient)
        List<User> users = repository.list()
        User user = users.find { it.email.toLowerCase() == forgotPasswordRequest.email.toLowerCase() }

        if (!user) {
            throw new RuntimeException("Unable to find user with email ${forgotPasswordRequest.email}")
        }
        if (!user.active) {
            throw new RuntimeException("User is inactive")
        }
        ForgotPasswordLink forgotPasswordLink = new ForgotPasswordLink(username: user.username, tenantId: forgotPasswordRequest.tenantId)
        Repository<ForgotPasswordLink> forgotPasswordLinkRepository = new FastDatastoreRepository<>(ForgotPasswordLink.class, internalHttpClient)
        forgotPasswordLink = forgotPasswordLinkRepository.create(forgotPasswordLink)

        ForgotPasswordEmailer forgotPasswordEmailer = new ForgotPasswordEmailer(internalHttpClient)
        Email email = forgotPasswordEmailer.sendForgotPasswordEmail(forgotPasswordRequest.email, forgotPasswordLink.username, forgotPasswordLink.toResetUrl())
        return email != null
    }

    @Override
    void resetPassword(String tenantId, String resetId) {
        String token = getInternalToken(tenantId)
        SecureHttpClient internalHttpClient = new SecureHttpClientBase(singletonClient, new ObtainTokenFromParameter(token)) {}
        Repository<ForgotPasswordLink> forgotPasswordLinkRepository = new FastDatastoreRepository<>(ForgotPasswordLink.class, internalHttpClient)
        ForgotPasswordLink link = forgotPasswordLinkRepository.get(resetId)
        if (!link) {
            throw new RuntimeException("Invalid reset request")
        }
        if (link.expireDate.before(new Date())) {
            throw new RuntimeException("Reset link has expired")
        }
        String toPost = gson.toJson(["username": link.username, "tenantGuid": tenantId])
        singletonClient.post("https://auth.trevorism.com/user/reset", toPost)
        forgotPasswordLinkRepository.delete(resetId)
    }

    private String invokeTokenRequest(String json) {
        String result = singletonClient.post("https://auth.trevorism.com/token", json)
        if (result.startsWith("<html>"))
            throw new RuntimeException("Bad Request to get token")
        log.trace("Successful login, token: ${result}")
        return result
    }

    private String getInternalToken(String tenantId) {
        try {
            SecureHttpClient secureHttpClient = new SecureHttpClientBase(singletonClient, new ObtainTokenFromAuthServiceFromPropertiesFile()) {}
            String subject = propertiesProvider.getProperty("clientId")
            InternalTokenRequest tokenRequest = new InternalTokenRequest(subject: subject, tenantId: tenantId)
            return secureHttpClient.post("https://auth.trevorism.com/token/internal", gson.toJson(tokenRequest))
        } catch (Exception e) {
            log.error("Unable to get token", e)
        }
        return null
    }
}
