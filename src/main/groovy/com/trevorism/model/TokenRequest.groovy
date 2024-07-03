package com.trevorism.model

class TokenRequest {
    String id
    String password
    String type = "user"
    String tenantGuid

    static TokenRequest fromLoginRequest(LoginRequest request, String guid){
        return new TokenRequest(id: request.username, password: request.password, tenantGuid: guid)
    }
}
