package me.srikavin.quiz.repository

import android.util.Log
import me.srikavin.quiz.model.AnswerResponse
import me.srikavin.quiz.model.Quiz
import me.srikavin.quiz.model.QuizGameState
import me.srikavin.quiz.network.common.model.data.QuizAnswerModel
import me.srikavin.quiz.network.common.model.data.QuizQuestionModel
import me.srikavin.quiz.repository.local.LocalGameRepository
import me.srikavin.quiz.view.main.TAG
import java.io.Serializable

/**
 * A wrapper class that abstracts the data type of a GameId and ensured typesafety
 */
inline class GameID(
        /**
         * The actual game id stored as a string
         */
        val value: String
)

/**
 * A repository that handles creating games and communicating events to and from the app and the game
 * server. Supports remote and local servers.
 */
object GameRepository {
    private val localGameRepository = LocalGameRepository()
    private val remoteGameRepository = RemoteGameRepository()

    /**
     * Creates a game with the given quiz. If [remote] is set to `true`, the game is created on a
     * remote game server.
     */
    fun createGame(quiz: Quiz, handler: GameResponseHandler, remote: Boolean) {
        if (remote) {
            remoteGameRepository.createGame(quiz, handler)
        } else {
            localGameRepository.createGame(quiz, handler)
        }
    }

    /**
     * Submits an answer to the underlying game with the given game id.
     */
    fun submitAnswer(id: GameID, answer: QuizAnswerModel) {
        if (localGameRepository.hasGame(id)) {
            localGameRepository.submitAnswer(id, answer)
        }
        if (remoteGameRepository.hasGame(id)) {
            remoteGameRepository.submitAnswer(id, answer)
        }
    }

    /**
     * Quits the given game. Rejoining is not possible.
     */
    fun quit(id: GameID) {
        if (localGameRepository.hasGame(id)) {
            localGameRepository.quit(id)
        }
        if (remoteGameRepository.hasGame(id)) {
            remoteGameRepository.quit(id)
        }
    }

    /**
     * Sends a signal to the backing repository to stop matchmaking
     * If offline, this has no effect
     */
    fun stopMatchmaking() {
        localGameRepository.stopMatchmaking()
    }

    /**
     * A enumeration of possible errors that may occur when using this repository
     */
    enum class ErrorCodes constructor(private val code: Int) {
        /**
         * Indicates an unknown error occurred
         */
        UNKNOWN_ERROR(0),
        /**
         * Indicates a network error occurred
         */
        NETWORK_ERROR(5),
        /**
         * Indicates a server error occurred
         */
        SERVER_ERROR(6);


        companion object {
            internal fun fromCode(errorCode: Int): ErrorCodes {
                for (e in values()) {
                    if (errorCode == e.code) {
                        return e
                    }
                }
                return UNKNOWN_ERROR
            }
        }
    }

    internal interface GameService {
        fun stopMatchmaking()

        fun createGame(quiz: Quiz, handler: GameResponseHandler)

        fun submitAnswer(id: GameID, answer: QuizAnswerModel?)

        fun hasGame(id: GameID): Boolean

        fun quit(id: GameID)
    }

    /**
     * An interface that should be used to handle events dispatched by a [GameRepository]. Not all
     * methods need to be overridden.
     */
    abstract class GameResponseHandler {
        /**
         * Handler for displaying the correct answer
         */
        open fun handleAnswer(response: AnswerResponse) {
            //By default, do nothing
        }

        /**
         * Handler for a new question or a change in current question
         */
        open fun handleQuestion(question: QuizQuestionModel) {
            //By default, do nothing
        }


        /**
         * Handler for updates to the GameInfo
         */
        open fun handleGameInfo(info: GameRepository.GameInfo) {

        }


        /**
         * Handler called when a new game is created with the game's id
         */
        open fun handleGameCreate(id: GameID) {

        }

        /**
         * Handler for Game Stats update
         */
        open fun handleGameStats(stats: GameStats) {

        }

        /**
         * Handler for new score update
         */
        open fun handleScoreChange(score: Int) {

        }

        /**
         * Handler for changes to the game state
         */
        open fun handleGameStateChange(state: QuizGameState) {

        }

        /**
         * Handler for changes to the time remaining for the current question
         */
        open fun handleGameTimeChange(timeLeft: Int) {

        }

        /**
         * Called when an exception occurs. These will be categorized into [ErrorCodes]. Unknown errors
         * will be returned as [ErrorCodes.UNKNOWN_ERROR].
         */
        fun handleErrors(vararg errors: ErrorCodes) {
            //By default, print error codes
            for (e in errors) {
                Log.w(TAG, "Ignored error code: " + e.name)
            }
        }
    }

    /**
     * Data class for holding the final game statistics
     */
    class GameStats(
            /**
             * The number of correct answers
             */
            val correct: Int,
            /**
             * The final score of the game player
             */
            val score: Int,
            /**
             * A list of answers chosen by the player. Null is given if no answer was chosen/
             */
            val chosen: List<QuizAnswerModel?>,
            /**
             * A list of questions present in the quiz
             */
            val quizQuestions: List<QuizQuestionModel>
    ) : Serializable {
        /**
         * The total number of quiz questions present in the quiz
         */
        val total: Int = quizQuestions.size
        /**
         * The percentage of questions the quiz player answered correctly
         */
        val percentCorrect: Double

        init {
            percentCorrect = if (total != 0) {
                correct / total.toDouble()
            } else {
                0.0
            }
        }
    }

    /**
     * A data class containing information about the quiz-game itself
     */
    class GameInfo(
            /**
             * The number of time taken for each question
             */
            val timePerQuestion: Int,
            /**
             * The number of questions present in this quiz
             */
            val numberOfQuestions: Int
    )

}
