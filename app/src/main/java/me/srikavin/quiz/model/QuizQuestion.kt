package me.srikavin.quiz.model

import android.media.Image
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import me.srikavin.quiz.network.common.model.data.QuizQuestionModel
import me.srikavin.quiz.network.common.model.data.ResourceId
import java.util.*

/**
 * A model class representing a quiz question
 */
data class QuizQuestion(
        /**
         * A unique identifier used to represent a specific quiz question
         */
        override val id: ResourceId = ResourceId(UUID.randomUUID().toString()),
        /**
         * The contents of this question (i.e. the question itself)
         */
        @Expose @SerializedName("text") override var contents: String = "",
        /**
         * A list of possible answers to this question
         */
        @Expose override var answers: MutableList<QuizAnswer> = mutableListOf(),
        internal var image: Image? = null
) : QuizQuestionModel
