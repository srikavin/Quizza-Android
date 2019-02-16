package me.srikavin.quiz.model

import com.google.gson.annotations.Expose

class AuthUser {
    @Expose
    val isAuth: Boolean = false
    @Expose
    val token: String? = null

}
