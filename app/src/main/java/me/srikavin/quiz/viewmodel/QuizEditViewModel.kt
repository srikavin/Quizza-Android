package me.srikavin.quiz.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import me.srikavin.quiz.model.Quiz
import me.srikavin.quiz.repository.QuizRepository

class QuizEditViewModel(application: Application) : AndroidViewModel(application) {
    private var quiz: MutableLiveData<Quiz>? = null
    private var creatingQuiz: Boolean = false
    private val quizRepository: QuizRepository = QuizRepository(application)

    fun createQuiz(): LiveData<Quiz> {
        quiz = MutableLiveData()
        val toSet = Quiz()
        toSet.draft = true
        quiz!!.postValue(toSet)
        creatingQuiz = true
        return quiz as MutableLiveData<Quiz>
    }

    fun editQuiz(id: String): LiveData<Quiz> {
        quiz = MutableLiveData()
        creatingQuiz = false

        quizRepository.getQuizByID(id, object : QuizRepository.QuizResponseHandler() {
            override fun handle(newQuiz: Quiz?) {
                quiz!!.postValue(newQuiz)
            }
        })

        return quiz as MutableLiveData<Quiz>
    }

    fun saveQuiz(): LiveData<Quiz> {
        val liveData = MutableLiveData<Quiz>()
        val quizVal = quiz!!.value
        if (quizVal == null) {
            liveData.postValue(null)
            return liveData
        }
        if (creatingQuiz) {
            quizRepository.createQuiz(quizVal, object : QuizRepository.QuizResponseHandler() {
                override fun handle(quiz: Quiz?) {
                    liveData.postValue(quiz)
                }
            })
        } else {
            quizRepository.editQuiz(quizVal.id.idString, quizVal, object : QuizRepository.QuizResponseHandler() {
                override fun handle(quiz: Quiz?) {
                    liveData.postValue(quiz)
                }
            })
        }

        return liveData
    }
}
