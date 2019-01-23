package me.srikavin.quiz.model

/**
 * A Model class that holds the structure of an answer response as returned by a repository
 */
class AnswerResponse(val isCorrect: Boolean, private val question: QuizQuestion, val correctAnswers: List<QuizAnswer>)
