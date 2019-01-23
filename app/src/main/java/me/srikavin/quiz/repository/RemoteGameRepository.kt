package me.srikavin.quiz.repository

import me.srikavin.quiz.model.Quiz
import me.srikavin.quiz.model.QuizAnswer
import java.io.BufferedReader
import java.io.BufferedWriter
import java.net.Socket

class RemoteGameRepository : GameRepository.GameService {
    lateinit var socket: Socket
    lateinit var writer: BufferedWriter
    lateinit var reader: BufferedReader
    val clientMap: Map<GameID, GameRepository.GameResponseHandler> = HashMap(2)

    private fun initSocket() {
        if (::socket.isInitialized) {
            socket = Socket("quiz-dev-game.srikavin.me", 1255)
            writer = socket.getOutputStream().bufferedWriter()
            reader = socket.getInputStream().bufferedReader()
        }
    }

    override fun createGame(quiz: Quiz, handler: GameRepository.GameResponseHandler) {
        initSocket()
    }

    override fun submitAnswer(id: GameID, answer: QuizAnswer?) {
        initSocket()
    }
}