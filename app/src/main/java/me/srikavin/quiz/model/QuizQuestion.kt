package me.srikavin.quiz.model

import android.media.Image
import com.google.gson.annotations.Expose
import me.srikavin.quiz.network.common.model.data.QuizQuestionModel
import me.srikavin.quiz.network.common.model.data.ResourceId
import java.util.*

data class QuizQuestion(
        override val id: ResourceId = ResourceId(UUID.randomUUID().toString()),
        @Expose override var contents: String = "",
        @Expose override var answers: MutableList<QuizAnswer> = mutableListOf(),
        var image: Image? = null
) : QuizQuestionModel
