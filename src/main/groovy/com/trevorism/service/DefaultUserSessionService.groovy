package com.trevorism.service

import com.google.gson.Gson
import com.trevorism.ClaimProperties
import com.trevorism.ClaimsProvider
import com.trevorism.ClasspathBasedPropertiesProvider
import com.trevorism.PropertiesProvider
import com.trevorism.data.FastDatastoreRepository
import com.trevorism.data.Repository
import com.trevorism.https.AppClientSecureHttpClient
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.ForgotPasswordLink
import com.trevorism.model.ForgotPasswordRequest
import com.trevorism.model.LoginRequest
import com.trevorism.model.TokenRequest
import com.trevorism.model.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@jakarta.inject.Singleton
class DefaultUserSessionService implements UserSessionService {

    private static final Logger log = LoggerFactory.getLogger(DefaultUserSessionService.class.getName())


    private SecureHttpClient secureHttpClient
    private Repository<ForgotPasswordLink> forgotPasswordLinkRepository
    private Repository<User> repository
    private PropertiesProvider propertiesProvider = new ClasspathBasedPropertiesProvider()
    private ForgotPasswordEmailer forgotPasswordEmailer;

    private final Gson gson = new Gson()

    DefaultUserSessionService(){
        this.secureHttpClient = new AppClientSecureHttpClient()
        forgotPasswordLinkRepository = new FastDatastoreRepository<>(ForgotPasswordLink.class, secureHttpClient)
        repository = new FastDatastoreRepository<>(User, secureHttpClient)
        this.forgotPasswordEmailer = new ForgotPasswordEmailer(secureHttpClient)
    }

    @Override
    String getToken(LoginRequest loginRequest) {
        String json = gson.toJson(TokenRequest.fromLoginRequest(loginRequest))
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
            def response = secureHttpClient.get("https://auth.trevorism.com/user/${claimProperties.id}", ["Authorization": "bearer ${token}".toString()])
            User user = gson.fromJson(response.value, User)
            return user
        } catch (Exception e) {
            log.warn("Unable to find user", e)
            return User.NULL_USER
        }
    }


    @Override
    void generateForgotPasswordLink(ForgotPasswordRequest forgotPasswordRequest) {
        List<User> users = repository.list()
        User user = users.find { it.email.toLowerCase() == forgotPasswordRequest.email.toLowerCase() }

        if (!user) {
            throw new RuntimeException("Unable to find user with email ${forgotPasswordRequest.email}")
        }
        if (!user.active) {
            throw new RuntimeException("User is inactive")
        }
        ForgotPasswordLink forgotPasswordLink = new ForgotPasswordLink(username: user.username)

        forgotPasswordLink = forgotPasswordLinkRepository.create(forgotPasswordLink)
        .sendForgotPasswordEmail(forgotPasswordRequest.email, forgotPasswordLink.username, forgotPasswordLink.toResetUrl())
    }

    @Override
    void resetPassword(String resetId) {
        ForgotPasswordLink link = forgotPasswordLinkRepository.get(resetId)
        if (!link) {
            throw new RuntimeException("Invalid reset request")
        }
        if (link.expireDate.before(new Date())) {
            throw new RuntimeException("Reset link has expired")
        }
        String toPost = gson.toJson(["username": link.username])
        secureHttpClient.post("https://auth.trevorism.com/user/reset", toPost)
        forgotPasswordLinkRepository.delete(resetId)
    }

    private String invokeTokenRequest(String json) {
        String result = secureHttpClient.post("https://auth.trevorism.com/token", json, [:]).value
        if (result.startsWith("<html>"))
            throw new RuntimeException("Bad Request to get token")
        log.trace("Successful login, token: ${result}")
        return result
    }
}
