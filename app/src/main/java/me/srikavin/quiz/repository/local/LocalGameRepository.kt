package me.srikavin.quiz.repository.local

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.srikavin.quiz.model.AnswerResponse
import me.srikavin.quiz.model.Game
import me.srikavin.quiz.model.Quiz
import me.srikavin.quiz.model.QuizGameState
import me.srikavin.quiz.network.common.model.data.QuizAnswerModel
import me.srikavin.quiz.repository.GameID
import me.srikavin.quiz.repository.GameRepository
import java.util.*

/**
 * An implementation of [GameRepository.GameService] that runs locally without an online game server.
 * These operations run entirely on the client side without internet connections. However, a quiz
 * instance must be available to create a game.
 */
class LocalGameRepository : GameRepository.GameService {
    /**
     *
     */
    override fun quit(id: GameID) {
        idGameMap.remove(id)
    }

    private val idGameMap = HashMap<GameID, Game>()

    override fun createGame(quiz: Quiz, handler: GameRepository.GameResponseHandler) {
        val id = GameID("${UUID.randomUUID()}-local")
        val game = Game(id, quiz, handler)

        idGameMap[id] = game
        game.state = QuizGameState.WAITING

        handler.handleGameCreate(id)
        handler.handleGameInfo(GameRepository.GameInfo(30, quiz.questions.size))

        game.state = QuizGameState.IN_PROGRESS
        nextQuestion(game)
    }

    /**
     * Stops matchmaking if applicable
     */
    override fun stopMatchmaking() {
        // Do nothing
    }

    override fun submitAnswer(id: GameID, answer: QuizAnswerModel?) {
        if (idGameMap[id] == null) {
            return
        }

        val game = idGameMap[id]!!

        if (game.waitingForAnswer || game.state == QuizGameState.FINISHED) {
            return
        }

        val correct = answer != null && answer.isCorrect

        game.chosen.add(answer)

        if (correct) {
            game.correct++
            game.score += 150
            game.handler.handleScoreChange(game.score)
        }

        val cur = game.quiz.questions[game.currentQuestion]
        val correctAnswers = cur.answers.filter { it.isCorrect }

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
                repeat(maxTime + 1) { time ->
                    if (curItem != game.currentQuestion) {
                        coroutineContext.cancel()
                        return@repeat
                    }
                    if (time >= maxTime) {
                        submitAnswer(game.id, null)
                        coroutineContext.cancel()
                    }
                    game.handler.handleGameTimeChange(maxTime - time)
                    delay(1000)
                }
            }
        } else {
            game.currentQuestion--
            game.state = QuizGameState.FINISHED
            game.handler.handleGameStats(GameRepository.GameStats(game.correct, game.score, game.chosen, game.quiz.questions))
        }
    }

    override fun hasGame(id: GameID): Boolean {
        return idGameMap.containsKey(id)
    }

}