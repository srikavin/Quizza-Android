package me.srikavin.quiz.model

import me.srikavin.quiz.network.common.model.data.QuizAnswerModel
import me.srikavin.quiz.repository.GameID
import me.srikavin.quiz.repository.GameRepository
import java.util.*

/**
 * Represents a game as provided by implementations of [GameRepository]
 */
class Game(
        /**
         * A unique identifier representing the game
         */
        val id: GameID,
        /**
         * The quiz this game is playing
         */
        val quiz: Quiz,
        /**
         * The handler used to handle game event updates
         */
        var handler: GameRepository.GameResponseHandler
) {
    /**
     * The total number of questions contained in the quiz
     */
    val total: Int = quiz.questions.size
    /**
     * A list of answers chosen by the game player
     */
    val chosen: MutableList<QuizAnswerModel?> = ArrayList()
    /**
     * The current score held by the game player
     */
    var score: Int = 0
    /**
     * The current number of correct answers received from the game player
     */
    var correct = 0

    internal var waitingForAnswer = false

    /**
     * The current game state
     */
    var state: QuizGameState = QuizGameState.WAITING
        set(value) {
            field = value
            handler.handleGameStateChange(value)
        }

    /**
     * The index of the current question
     */
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