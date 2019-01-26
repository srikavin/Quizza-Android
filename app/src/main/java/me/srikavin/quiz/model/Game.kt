package me.srikavin.quiz.model

import me.srikavin.quiz.repository.GameID
import me.srikavin.quiz.repository.GameRepository
import java.util.*

class Game(val id: GameID, val quiz: Quiz, var handler: GameRepository.GameResponseHandler) {
    val total: Int = quiz.questions.size
    val chosen: MutableList<QuizAnswer?> = ArrayList()
    var score: Int = 0
    var correct = 0
    var waitingForAnswer = false

    var state: QuizGameState = QuizGameState.WAITING_FOR_PLAYERS
        set(value) {
            field = value
            handler.handleGameStateChange(value)
        }

    var currentQuestion = -1
        set(value) {
            field = value
            state.currentQuestion = value
            this.state = state
        }

    init {
        quiz.questions.shuffle()
    }
}