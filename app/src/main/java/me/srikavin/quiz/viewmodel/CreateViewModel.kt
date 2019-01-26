package me.srikavin.quiz.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import me.srikavin.quiz.model.Quiz
import me.srikavin.quiz.repository.QuizRepository

class CreateViewModel(application: Application) : AndroidViewModel(application) {
    private var ownedQuizzes: MutableLiveData<List<Quiz>>? = null
    private val quizRepository: QuizRepository

    init {
        quizRepository = QuizRepository(application)
    }

    fun getOwnedQuizzes(): LiveData<List<Quiz>> {
        if (ownedQuizzes == null) {
            ownedQuizzes = MutableLiveData()
            updateDrafts()
        }
        return ownedQuizzes as MutableLiveData<List<Quiz>>
    }

    fun deleteQuiz(quiz: Quiz): LiveData<List<Quiz>>? {
        quizRepository.deleteQuiz(quiz, object : QuizRepository.QuizResponseHandler() {
            override fun handle(user: Quiz?) {
                updateDrafts()
            }

        })
        return ownedQuizzes
    }

    fun updateDrafts() {
        quizRepository.getOwned(getApplication(), object : QuizRepository.QuizResponseHandler() {
            override fun handleMultiple(newQuizzes: List<Quiz>) {
                ownedQuizzes!!.postValue(newQuizzes)
            }
        })
    }
}
