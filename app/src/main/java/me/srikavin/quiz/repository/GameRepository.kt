package me.srikavin.quiz.repository

import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.srikavin.quiz.MainActivity.TAG
import me.srikavin.quiz.model.*
import java.util.*

inline class GameID(val value: String)

enum class GameRepository {
    INSTANCE;


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

    class LocalGameRepository : GameService {
        private val idGameMap = HashMap<GameID, Game>()

        override fun createGame(quiz: Quiz, handler: GameResponseHandler) {
            val id = GameID(UUID.randomUUID().toString())
            val game = Game(id, quiz, handler)
            idGameMap[id] = game
            game.state = QuizGameState.WAITING_FOR_PLAYERS
            handler.handleGameCreate(id, GameInfo(30, quiz.questions.size))
            game.state = QuizGameState.IN_PROGRESS
            nextQuestion(game)
        }

        override fun submitAnswer(id: GameID, answer: QuizAnswer?) {
            val game = idGameMap[id]!!

            if (game.waitingForAnswer || game.state == QuizGameState.FINISHED) {
                return
            }

            val correct = answer != null && answer.correct

            game.chosen.add(answer)

            if (correct) {
                game.correct++
                game.score += 150
                game.handler.handleScoreChange(game.score)
            }

            val cur = game.quiz.questions[game.currentQuestion]
            val correctAnswers = cur.answers.filter { it.correct }

            game.handler.handleAnswer(AnswerResponse(correct, cur, correctAnswers))

            game.waitingForAnswer = true
            // Delay the passage of the next question to allow for animations and effects

            GlobalScope.launch {
                delay(1500)
                nextQuestion(game)
                game.waitingForAnswer = false
            }
        }

        private fun nextQuestion(game: Game) {
            game.currentQuestion++
            if (game.currentQuestion < game.total) {
                game.currentQuestion = game.currentQuestion

                val cur = game.quiz.questions[game.currentQuestion]
                cur.answers.shuffle()

                game.handler.handleQuestion(cur)

                val maxTime = 30

                GlobalScope.launch {
                    val curItem = game.currentQuestion
                    repeat(maxTime) { time ->
                        if (time >= maxTime) {
                            submitAnswer(game.id, null)
                            coroutineContext.cancel()
                        }
                        if (curItem != game.currentQuestion) {
                            coroutineContext.cancel()
                        }
                        game.handler.handleGameTimeChange(maxTime - time)
                        delay(1000)
                    }
                }
            } else {
                game.currentQuestion--
                game.state = QuizGameState.FINISHED
                game.handler.handleGameStats(GameStats(game.correct, game.score, game.chosen, game.quiz.questions))
            }
        }

        internal inner class Game(var id: GameID, var quiz: Quiz, var handler: GameResponseHandler) {
            var total: Int = 0
            var score: Int = 0
            var correct = 0
            var waitingForAnswer = false
            var chosen: MutableList<QuizAnswer?> = ArrayList()

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
                total = quiz.questions.size
            }
        }
    }
}
