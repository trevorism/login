package com.trevorism.model

import groovy.transform.ToString

@ToString
class User {

    String id
    String username
    String email
    String image
    boolean admin
    boolean active

    static User NULL_USER = new User()
    static boolean isNullUser(User user) {
        return !(user?.username)
    }
}
