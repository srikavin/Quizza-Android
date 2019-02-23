package me.srikavin.quiz.repository

import kotlinx.coroutines.*
import me.srikavin.quiz.model.AnswerResponse
import me.srikavin.quiz.model.Quiz
import me.srikavin.quiz.model.QuizGameState
import me.srikavin.quiz.network.client.NetworkClient
import me.srikavin.quiz.network.client.NetworkGameClient
import me.srikavin.quiz.network.common.MessageHandler
import me.srikavin.quiz.network.common.MessageRouter
import me.srikavin.quiz.network.common.message.GAME_END_PACKET_ID
import me.srikavin.quiz.network.common.message.game.GameEndMessage
import me.srikavin.quiz.network.common.message.game.GameState
import me.srikavin.quiz.network.common.model.data.QuizAnswerModel
import me.srikavin.quiz.network.common.model.game.GameClient
import me.srikavin.quiz.network.common.model.matchmaker.MatchmakerStates
import me.srikavin.quiz.network.common.model.network.RejoinToken
import org.threeten.bp.Instant
import java.io.IOException
import java.net.InetAddress
import java.util.*

private fun createExceptionHandler(handler: GameRepository.GameResponseHandler): CoroutineExceptionHandler {
    return CoroutineExceptionHandler { _, exception ->
        exception.printStackTrace()
        if (exception is IOException) {
            handler.handleErrors(GameRepository.ErrorCodes.NETWORK_ERROR)
        } else {
            handler.handleErrors(GameRepository.ErrorCodes.UNKNOWN_ERROR)
        }
    }
}

private fun calculateTimeLeft(instant: Instant): Long {
    return (instant.toEpochMilli() - Instant.now().toEpochMilli()) / 1000
}

class RemoteGameRepository : GameRepository.GameService {
    override fun hasGame(id: GameID): Boolean {
        return gameMap.containsKey(id)
    }

    override fun quit(id: GameID) {
        stopMatchmaking()
        gameMap.remove(id)
        gameMap[id]?.gameClient?.shutdown()
        gameMap[id]?.gameClient?.onMatchmakingStateUpdate = {}
        gameMap[id]?.gameClient?.onGameStateUpdate = {}
        gameMap[id]?.timeLeftJob?.cancel()
    }

    private data class GameItem(
            val handler: GameRepository.GameResponseHandler,
            val router: MessageRouter,
            val gameClient: NetworkGameClient,
            var timeLeftJob: Job?,
            val rejoinToken: RejoinToken,
            var state: GameState?,
            var submittedAnswer: Boolean = false,
            var numberCorrect: Int = 0,
            val chosen: MutableList<QuizAnswerModel> = mutableListOf()
    )

    private val gameMap: MutableMap<GameID, GameItem> = mutableMapOf()

    private suspend fun handleGameTimer(handler: GameRepository.GameResponseHandler, timeLeft: Long) {
        var t = 30
        while (t > 0) {
            t = ((timeLeft - Instant.now().toEpochMilli()) / 1000).toInt()
            handler.handleGameTimeChange(t)
            delay(500)
        }
    }

    override fun stopMatchmaking() {
        for (e: GameItem in gameMap.values) {
            if (e.state?.quiz == null) {
                e.gameClient.stopMatchmaking()
            }
        }
    }

    override fun createGame(quiz: Quiz, handler: GameRepository.GameResponseHandler) {
        val exceptionHandler = createExceptionHandler(handler)

        GlobalScope.launch(exceptionHandler) {
            println("launching matchmaker and client")
            val messageRouter = MessageRouter()

            val client = NetworkClient(InetAddress.getByName("quiz-game.srikavin.me"), messageRouter, exceptionHandler = exceptionHandler)
            client.start(CoroutineScope(Dispatchers.IO), null as UUID?)

            val gameID = GameID(UUID.randomUUID().toString())

            val gameClient = NetworkGameClient(client, messageRouter)


            val game = GameItem(handler, messageRouter, gameClient, null, client.rejoinToken, null)
            println(game)

            gameMap[gameID] = game


            messageRouter.registerHandler(GAME_END_PACKET_ID, object : MessageHandler<GameEndMessage> {
                override fun handle(client: GameClient, message: GameEndMessage) {
                    val stats = GameRepository.GameStats(
                            game.numberCorrect,
                            game.state?.players?.find { it.id == client.backing.id }?.score ?: 0,
                            game.chosen,
                            game.state?.quiz?.questions ?: mutableListOf()
                    )
                    handler.handleGameStateChange(QuizGameState.FINISHED)
                    handler.handleGameStats(stats)
                    println("Handling game stats")
                }
            })

            gameClient.onMatchmakingStateUpdate = { matchmakingState ->
                println(matchmakingState)
                println(matchmakingState.state)
                if (matchmakingState.state == MatchmakerStates.MATCH_FOUND) {
                    handler.handleGameCreate(gameID)
                }
            }

            gameClient.onGameStateUpdate = { gameState ->
                if (game.state?.currentQuestion != gameState.currentQuestion) {
                    game.submittedAnswer = false
                }

                game.timeLeftJob?.cancel()
                game.timeLeftJob = GlobalScope.launch(exceptionHandler) {
                    handleGameTimer(handler, gameState.timeLeft.toEpochMilli())
                }
                handler.handleQuestion(gameState.quiz.questions[gameState.currentQuestion])
                handler.handleGameInfo(GameRepository.GameInfo(calculateTimeLeft(gameState.timeLeft).toInt(), gameState.quiz.questions.size))
                handler.handleGameStateChange(QuizGameState.IN_PROGRESS.apply { currentQuestion = gameState.currentQuestion })

                val score = gameState.players.find { it.id == client.userId.id }?.score
                score?.let {
                    handler.handleScoreChange(it)
                }

                game.state = gameState
            }
            gameClient.startMatchmaking(quiz.id)
        }
    }


    override fun submitAnswer(id: GameID, answer: QuizAnswerModel?) {
        answer ?: return
        val game = gameMap[id] ?: return

        if (game.submittedAnswer) {
            return
        }

        val state = game.state ?: return

        val curQuestion = state.quiz.questions[state.currentQuestion]
        game.gameClient.sendAnswer(answer.id)
        game.handler.handleAnswer(AnswerResponse(
                answer.isCorrect,
                curQuestion,
                curQuestion.answers.filter { it.isCorrect }
        ))

        if (answer.isCorrect) {
            game.numberCorrect++
        }
        game.chosen.add(answer)
        game.submittedAnswer = true

    }
}