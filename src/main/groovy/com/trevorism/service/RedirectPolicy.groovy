package com.trevorism.service

class RedirectPolicy {

    static final String PLATFORM_DOMAIN = "trevorism.com"
    static final String DEFAULT_RETURN_URL = "https://trevorism.com"
    static final String CALLBACK_PATH = "/api/auth/callback"

    static boolean isPlatformReturnUrl(String returnUrl) {
        URI uri = parse(returnUrl)
        if (!uri) {
            return false
        }
        String host = uri.host?.toLowerCase()
        if (!host || uri.scheme?.toLowerCase() != "https" || uri.rawUserInfo != null) {
            return false
        }
        return host == PLATFORM_DOMAIN || host.endsWith(".${PLATFORM_DOMAIN}")
    }

    static String safeReturnUrl(String returnUrl) {
        return isPlatformReturnUrl(returnUrl) ? returnUrl : DEFAULT_RETURN_URL
    }

    static String callbackUriForOrigin(String origin) {
        URI uri = parse(origin)
        if (!uri?.host || !uri.scheme || uri.rawUserInfo != null) {
            return null
        }
        int port = uri.port
        String authority = port == -1 ? uri.host : "${uri.host}:${port}"
        return "${uri.scheme.toLowerCase()}://${authority.toLowerCase()}${CALLBACK_PATH}"
    }

    private static URI parse(String value) {
        if (!value) {
            return null
        }
        try {
            return new URI(value)
        } catch (URISyntaxException ignored) {
            return null
        }
    }
}
