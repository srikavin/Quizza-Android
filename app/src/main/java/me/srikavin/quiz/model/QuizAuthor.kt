package me.srikavin.quiz.model

import com.google.gson.annotations.Expose
import me.srikavin.quiz.network.common.model.data.ResourceId

/**
 * A model class representing the author of a quiz
 */
data class QuizAuthor(
        /**
         * A unique identifier used to represent a specific quiz author
         */
        @Expose val id: ResourceId,
        /**
         * The quiz author's username
         */
        @Expose val username: String
)