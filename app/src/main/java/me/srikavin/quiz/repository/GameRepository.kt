package me.srikavin.quiz.repository

import android.util.Log
import me.srikavin.quiz.model.*
import me.srikavin.quiz.repository.local.LocalGameRepository
import me.srikavin.quiz.view.main.MainActivity.TAG

inline class GameID(val value: String)

object GameRepository {
    private val localGameRepository = LocalGameRepository()

    fun createGame(quiz: Quiz, handler: GameResponseHandler) {
        localGameRepository.createGame(quiz, handler)
    }

    fun submitAnswer(id: GameID, answer: QuizAnswer) {
        localGameRepository.submitAnswer(id, answer)
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
        fun createGame(quiz: Quiz, handler: GameResponseHandler)

        fun submitAnswer(id: GameID, answer: QuizAnswer?)
    }

    abstract class GameResponseHandler {
        open fun handleAnswer(response: AnswerResponse) {
            //By default, do nothing
        }

        open fun handleQuestion(question: QuizQuestion) {
            //By default, do nothing
        }

        open fun handleGameCreate(id: GameID, info: GameInfo) {
            //By default, do nothing
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

    class GameStats(val correct: Int, val score: Int, val chosen: List<QuizAnswer?>, val quizQuestions: List<QuizQuestion>) {
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
