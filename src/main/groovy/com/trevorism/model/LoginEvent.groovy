package com.trevorism.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder

class LoginEvent {
    private static Gson gson = new GsonBuilder().disableHtmlEscaping().setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").create()

    String username
    String tenantId
    boolean success
    Date date = new Date()

    static String createEventJson(String username, String tenantId, boolean success) {
        LoginEvent event = new LoginEvent()
        event.username = username
        event.tenantId = tenantId
        event.success = success
        return gson.toJson(event)
    }


}
