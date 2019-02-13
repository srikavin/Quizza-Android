package me.srikavin.quiz.model

import me.srikavin.quiz.network.common.model.data.QuizAnswerModel

/**
 * A Model class that holds the structure of an answer response as returned by a repository
 */
class AnswerResponse(val isCorrect: Boolean, private val question: QuizQuestion, val correctAnswers: List<QuizAnswerModel>)
