package me.srikavin.quiz.model

import android.media.Image

import com.google.gson.annotations.Expose

class UserProfile {
    @Expose
    private val id: String? = null
    @Expose
    val avatar: Image? = null
    @Expose
    val username: String? = null
}
