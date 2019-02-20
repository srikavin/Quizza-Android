package me.srikavin.quiz.model

import android.media.Image

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import me.srikavin.quiz.network.common.model.data.QuizAnswerModel
import me.srikavin.quiz.network.common.model.data.ResourceId
import java.util.*

/**
 * A model class representing a quiz answer
 */
data class QuizAnswer(
        /**
         * A unique identifier used to represent a specific quiz answer
         */
        @Expose override var id: ResourceId = ResourceId(UUID.randomUUID().toString()),
        /**
         * The text contained withing the answer
         */
        @Expose @SerializedName("text") override var contents: String = "",
        /**
         * Whether or not the answer is correct
         */
        @Expose @SerializedName("correct") override var isCorrect: Boolean = false,
        @Expose internal var image: Image? = null
) : QuizAnswerModel
