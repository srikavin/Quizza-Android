package me.srikavin.quiz.model

import com.google.gson.annotations.Expose

/**
 * A container class representing the authentication token and the authentication state
 */
class AuthUser {
    /**
     * Represents whether the user is authenticated
     */
    @Expose
    val isAuth: Boolean = false
    /**
     * Contains the token that will be necessary for any further requests to the server
     */
    @Expose
    val token: String? = null
}
