package me.srikavin.quiz.model

enum class QuizGameState {
    WAITING,
    IN_PROGRESS,
    FINISHED;

    var currentQuestion: Int = 0
}
