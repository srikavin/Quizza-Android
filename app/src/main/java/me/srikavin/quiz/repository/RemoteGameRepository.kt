package me.srikavin.quiz.repository

import kotlinx.coroutines.*
import me.srikavin.quiz.model.Quiz
import me.srikavin.quiz.network.client.NetworkClient
import me.srikavin.quiz.network.client.NetworkGameClient
import me.srikavin.quiz.network.common.MessageRouter
import me.srikavin.quiz.network.common.model.data.QuizAnswerModel
import me.srikavin.quiz.network.common.model.data.ResourceId
import me.srikavin.quiz.network.common.model.matchmaker.MatchmakerStates
import me.srikavin.quiz.network.common.model.network.RejoinToken
import org.threeten.bp.Instant
import java.net.InetAddress
import java.util.*

class RemoteGameRepository : GameRepository.GameService {
    private lateinit var gameClient: NetworkGameClient
    private val gameMap: MutableMap<GameID, RejoinToken> = mutableMapOf()
    private val router: MessageRouter = MessageRouter()
    private var handler: GameRepository.GameResponseHandler? = null
    private var timeLeftJob: Job? = null

    private suspend fun handleGameTimer(handler: GameRepository.GameResponseHandler, timeLeft: Long) {
        var t = 30
        while (t > 0) {
            t = ((timeLeft - Instant.now().toEpochMilli()) / 1000).toInt()
            handler.handleGameTimeChange(t)
            delay(500)
        }
    }

    override fun createGame(quiz: Quiz, handler: GameRepository.GameResponseHandler) {
        this.handler = handler
        GlobalScope.launch {
            val client = NetworkClient(InetAddress.getByName("quiz-dev-game.srikavin.me"), router)
            client.start(CoroutineScope(Dispatchers.IO), null as UUID?)

            val gameID = GameID(UUID.randomUUID().toString())

            gameMap.put(gameID, client.rejoinToken)


            println(client)
            gameClient = NetworkGameClient(client, router)

            gameClient.onMatchmakingStateUpdate = { matchmakingState ->
                if (matchmakingState.state == MatchmakerStates.MATCH_FOUND) {
                    handler.handleGameCreate(gameID, GameRepository.GameInfo(30, 1))
                }
            }

            gameClient.onGameStateUpdate = { gameState ->
                timeLeftJob?.cancel()
                timeLeftJob = GlobalScope.launch {
                    handleGameTimer(handler, gameState.timeLeft.toEpochMilli())
                }
                handler.handleQuestion(gameState.quiz.questions[gameState.currentQuestion])
            }
            gameClient.startMatchmaking(ResourceId(quiz.id))
        }
    }

    override fun submitAnswer(id: GameID, answer: QuizAnswerModel?) {
        answer ?: return
        gameClient.sendAnswer(answer.id)
    }
}