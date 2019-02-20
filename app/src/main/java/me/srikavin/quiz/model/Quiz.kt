package me.srikavin.quiz.model

import com.google.gson.annotations.Expose
import me.srikavin.quiz.network.common.model.data.QuizModel
import me.srikavin.quiz.network.common.model.data.ResourceId
import java.util.*

/**
 * A model class representing a quiz as provided by implementations of [me.srikavin.quiz.repository.QuizRepository]
 */
data class Quiz(
        /**
         * A unique identifier used to represent a specific quiz
         */
        @Expose(serialize = false)
        override var id: ResourceId = ResourceId(UUID.randomUUID().toString()),
        /**
         * The title of the quiz
         */
        @Expose override var title: String = "",
        /**
         * A brief description of the quiz
         */
        @Expose override var description: String = "",
        /**
         * A list of tags that may contain topics or events allowing quizzes to be grouped
         */
        @Expose var tags: List<Tag> = mutableListOf(),
        /**
         * A list of questions present withing this quiz
         */
        @Expose override var questions: MutableList<QuizQuestion> = mutableListOf(),
        /**
         * Whether or not this quiz is an in-progress draft
         */
        @Expose var draft: Boolean = true,
        /**
         * A short string that may be displayed before viewing quiz details
         */
        @Expose var overview: String = "",
        /**
         * A string containing a URL of an image to display
         */
        @Expose var coverImage: String? = null,
        /**
         * The author of this quiz
         */
        @Expose(serialize = false) val author: QuizAuthor? = null,
        internal var isLocal: Boolean = false
) : QuizModel

