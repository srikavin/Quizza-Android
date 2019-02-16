package me.srikavin.quiz.model

import me.srikavin.quiz.network.common.model.data.QuizAnswerModel
import me.srikavin.quiz.network.common.model.data.QuizQuestionModel

/**
 * A Model class that holds the structure of an answer response as returned by a repository
 */
class AnswerResponse(val isCorrect: Boolean, val question: QuizQuestionModel, val correctAnswers: List<QuizAnswerModel>)
