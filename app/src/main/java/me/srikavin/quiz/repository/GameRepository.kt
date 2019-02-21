package me.srikavin.quiz.repository

import android.util.Log
import me.srikavin.quiz.model.AnswerResponse
import me.srikavin.quiz.model.Quiz
import me.srikavin.quiz.model.QuizGameState
import me.srikavin.quiz.network.common.model.data.QuizAnswerModel
import me.srikavin.quiz.network.common.model.data.QuizQuestionModel
import me.srikavin.quiz.view.main.TAG
import java.io.Serializable

inline class GameID(val value: String)

object GameRepository {
    private val localGameRepository = RemoteGameRepository()

    fun createGame(quiz: Quiz, handler: GameResponseHandler) {
        localGameRepository.createGame(quiz, handler)
    }

    fun submitAnswer(id: GameID, answer: QuizAnswerModel) {
        localGameRepository.submitAnswer(id, answer)
    }

    fun quit(id: GameID) {
        localGameRepository.quit(id)
    }

    /**
     * Sends a signal to the backing repository to stop matchmaking
     * If offline, this has no effect
     */
    fun stopMatchmaking() {
        localGameRepository.stopMatchmaking()
    }

    enum class ErrorCodes constructor(private val code: Int) {
        UNKNOWN_ERROR(0),
        NETWORK_ERROR(5),
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

        fun quit(id: GameID)
    }

    abstract class GameResponseHandler {
        open fun handleAnswer(response: AnswerResponse) {
            //By default, do nothing
        }

        open fun handleQuestion(question: QuizQuestionModel) {
            //By default, do nothing
        }

        open fun handleGameInfo(info: GameRepository.GameInfo) {

        }


        open fun handleGameCreate(id: GameID) {

        }

        open fun handleGameStats(stats: GameStats) {

        }

        open fun handleScoreChange(score: Int) {

        }

        open fun handleGameStateChange(state: QuizGameState) {

        }

        open fun handleGameTimeChange(timeLeft: Int) {

        }

        fun handleErrors(vararg errors: ErrorCodes) {
            //By default, print error codes
            for (e in errors) {
                Log.w(TAG, "Ignored error code: " + e.name)
            }
        }
    }

    class GameStats(val correct: Int, val score: Int, val chosen: List<QuizAnswerModel?>, val quizQuestions: List<QuizQuestionModel>) : Serializable {
        val total: Int = quizQuestions.size
        val percentCorrect: Double

        init {
            percentCorrect = if (total != 0) {
                correct / total.toDouble()
            } else {
                0.0
            }
        }
    }

    class GameInfo(val timePerQuestion: Int, val numberOfQuestions: Int)

}
