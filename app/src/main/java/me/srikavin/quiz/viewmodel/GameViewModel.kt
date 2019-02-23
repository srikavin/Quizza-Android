package me.srikavin.quiz.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ww.roxie.BaseAction
import com.ww.roxie.BaseState
import me.srikavin.quiz.model.AnswerResponse
import me.srikavin.quiz.model.Quiz
import me.srikavin.quiz.model.QuizGameState
import me.srikavin.quiz.network.common.model.data.QuizAnswerModel
import me.srikavin.quiz.network.common.model.data.QuizQuestionModel
import me.srikavin.quiz.repository.GameID
import me.srikavin.quiz.repository.GameRepository
import me.srikavin.quiz.repository.QuizRepository

sealed class GameAction : BaseAction {
    data class LoadQuiz(val quizId: String) : GameAction()
}

sealed class GameChange {
    object Loading : GameChange()
    data class QuizLoad(val quiz: Quiz) : GameChange()
    data class Error(val error: Throwable?) : GameChange()
}

data class GameViewState(
        val quiz: Quiz? = null,
        val loading: Boolean = false,
        val error: Boolean = false
) : BaseState

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val quizRepository: QuizRepository = QuizRepository(application)

    private var quiz = MutableLiveData<Quiz>()

    private val gameScore = MutableLiveData<Int>()

    private val currentGameID = MutableLiveData<GameID>()
    private val gameInfo = MutableLiveData<GameRepository.GameInfo>()
    private val currentQuestion = MutableLiveData<QuizQuestionModel>()
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

    fun getCurrentQuestion(): LiveData<QuizQuestionModel> {
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

    fun stopMatchmaking() {
        GameRepository.stopMatchmaking()
    }

    fun createGame(quiz: Quiz, remote: Boolean) {
        if (createGame == null) {
            createGame = MutableLiveData()
            GameRepository.createGame(quiz, object : GameRepository.GameResponseHandler() {
                override fun handleAnswer(response: AnswerResponse) {
                    answerResponse.postValue(response)
                }

                override fun handleQuestion(question: QuizQuestionModel) {
                    currentQuestion.postValue(question)
                }

                override fun handleGameStateChange(state: QuizGameState) {
                    gameState.postValue(state)
                }

                override fun handleScoreChange(score: Int) {
                    gameScore.postValue(score)
                }

                override fun handleGameInfo(info: GameRepository.GameInfo) {
                    gameInfo.postValue(info)
                }

                override fun handleGameCreate(id: GameID) {
                    currentGameID.postValue(id)
                }

                override fun handleGameStats(stats: GameRepository.GameStats) {
                    gameStats.postValue(stats)
                }

                override fun handleGameTimeChange(timeLeft: Int) {
                    timeRemaining.postValue(timeLeft)

                }
            }, remote)
        }
        return
    }

    fun submitAnswer(quizAnswer: QuizAnswerModel) {
        GameRepository.submitAnswer(currentGameID.value
                ?: throw RuntimeException("Current game ID is null; cannot call submit answer here"), quizAnswer)
    }

    fun quitGame() {
        GameRepository.quit(currentGameID.value ?: return)
    }

    private fun loadQuizzes(id: String) {
        quizRepository.getQuizByID(id, object : QuizRepository.QuizResponseHandler() {
            override fun handle(quiz: Quiz?) {
                this@GameViewModel.quiz.postValue(quiz)
            }
        })
    }
}
