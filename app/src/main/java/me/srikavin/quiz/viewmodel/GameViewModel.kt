package me.srikavin.quiz.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import me.srikavin.quiz.model.*
import me.srikavin.quiz.repository.GameID
import me.srikavin.quiz.repository.GameRepository
import me.srikavin.quiz.repository.QuizRepository

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val quizRepository: QuizRepository = QuizRepository(application)

    private var quiz = MutableLiveData<Quiz>()

    private val gameScore = MutableLiveData<Int>()

    private val currentGameID = MutableLiveData<GameID>()
    private val gameInfo = MutableLiveData<GameRepository.GameInfo>()
    private val currentQuestion = MutableLiveData<QuizQuestion>()
    private val answerResponse = MutableLiveData<AnswerResponse>()
    private val gameStats = MutableLiveData<GameRepository.GameStats>()
    private val gameState = MutableLiveData<QuizGameState>()
    private val timeRemaining = MutableLiveData<Int>()
    private var createGame: MutableLiveData<Void>? = null


    fun getCurrentGameID(): LiveData<GameID> {
        return currentGameID
    }

    fun getGameInfo(): LiveData<GameRepository.GameInfo> {
        return gameInfo
    }

    fun getGameState(): LiveData<QuizGameState> {
        return gameState
    }

    fun getTimeRemaining(): LiveData<Int> {
        return timeRemaining
    }

    fun getCurrentQuestion(): LiveData<QuizQuestion> {
        return currentQuestion
    }

    fun getAnswerResponse(): LiveData<AnswerResponse> {
        return answerResponse
    }

    fun getGameStats(): LiveData<GameRepository.GameStats> {
        return gameStats
    }

    fun getGameScore(): LiveData<Int> {
        return gameScore
    }

    fun getQuizByID(id: String): LiveData<Quiz> {
        quiz = MutableLiveData()
        loadQuizzes(id)
        return quiz
    }

    fun createGame(quiz: Quiz) {
        if (createGame == null) {
            createGame = MutableLiveData()
            GameRepository.createGame(quiz, object : GameRepository.GameResponseHandler() {
                override fun handleAnswer(response: AnswerResponse) {
                    answerResponse.postValue(response)
                }

                override fun handleQuestion(question: QuizQuestion) {
                    currentQuestion.postValue(question)
                }

                override fun handleGameStateChange(state: QuizGameState) {
                    gameState.postValue(state)
                }

                override fun handleScoreChange(score: Int) {
                    gameScore.postValue(score)
                }

                override fun handleGameCreate(id: GameID, info: GameRepository.GameInfo) {
                    currentGameID.postValue(id)
                    gameInfo.postValue(info)
                }

                override fun handleGameStats(stats: GameRepository.GameStats) {
                    gameStats.postValue(stats)
                }

                override fun handleGameTimeChange(timeLeft: Int) {
                    timeRemaining.postValue(timeLeft)

                }
            })
        }
        return
    }

    fun submitAnswer(quizAnswer: QuizAnswer) {
        GameRepository.submitAnswer(currentGameID.value
                ?: throw RuntimeException("Current game ID is null; cannot call submit answer here"), quizAnswer)
    }

    private fun loadQuizzes(id: String) {
        quizRepository.getQuizByID(id, object : QuizRepository.QuizResponseHandler() {
            override fun handle(quiz: Quiz?) {
                this@GameViewModel.quiz.postValue(quiz)
            }
        })
    }
}
