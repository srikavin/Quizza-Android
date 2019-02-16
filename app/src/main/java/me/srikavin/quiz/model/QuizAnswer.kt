package me.srikavin.quiz.model

import android.media.Image

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import me.srikavin.quiz.network.common.model.data.QuizAnswerModel
import me.srikavin.quiz.network.common.model.data.ResourceId
import java.util.*

data class QuizAnswer(
        @Expose override var id: ResourceId = ResourceId(UUID.randomUUID().toString()),
        @Expose @SerializedName("text") override var contents: String = "",
        @Expose override var isCorrect: Boolean = false,
        @Expose var image: Image? = null
) : QuizAnswerModel
