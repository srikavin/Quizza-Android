package me.srikavin.quiz.model

import me.srikavin.quiz.network.common.model.data.QuizAnswerModel
import me.srikavin.quiz.network.common.model.data.QuizQuestionModel

/**
 * A Model class that holds the structure of an answer response as returned by a repository
 */
class AnswerResponse(
        /**
         * Represents whether the answer to the last question was correct
         */
        val isCorrect: Boolean,
        /**
         * The [QuizQuestionModel] representing the last question
         */
        val question: QuizQuestionModel,
        /**
         * A list of [QuizAnswerModel] containing all of the correct answers
         */
        val correctAnswers: List<QuizAnswerModel>
)
