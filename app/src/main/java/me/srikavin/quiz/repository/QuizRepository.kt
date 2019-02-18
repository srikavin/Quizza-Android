package me.srikavin.quiz.repository

import android.content.Context
import android.util.Log
import io.reactivex.Completable
import io.reactivex.Single
import me.srikavin.quiz.model.Quiz
import me.srikavin.quiz.repository.internet.InternetQuizRepository
import me.srikavin.quiz.repository.local.LocalQuizRepository
import me.srikavin.quiz.view.main.TAG

class QuizRepository(private val context: Context) {
    private val quizRepository: QuizService = LocalQuizRepository(InternetQuizRepository())

    fun getQuizByID(id: String): Single<Quiz> {
        return quizRepository.getQuizByID(id)
    }

    fun getQuizByID(id: String, handler: QuizResponseHandler) {
        return quizRepository.getQuizByID(id, handler)
    }

    fun getQuizzes(): Single<List<Quiz>> {
        return quizRepository.getQuizzes()
    }

    fun getQuizzes(handler: QuizResponseHandler) {
        quizRepository.getQuizzes(handler)
    }

    fun getOwned(): Single<List<Quiz>> {
        return quizRepository.getOwned(context)
    }

    fun getOwned(handler: QuizResponseHandler) {
        quizRepository.getOwned(context, handler)
    }

    fun createQuiz(quiz: Quiz, handler: QuizResponseHandler) {
        quizRepository.createQuiz(context, quiz, handler)
    }

    fun editQuiz(id: String, quiz: Quiz, handler: QuizResponseHandler) {
        quizRepository.editQuiz(context, id, quiz, handler)
    }

    fun deleteQuiz(quiz: Quiz): Completable {
        return quizRepository.deleteQuiz(context, quiz)
    }

    fun deleteQuiz(quiz: Quiz, handler: QuizResponseHandler) {
        quizRepository.deleteQuiz(context, quiz, handler)
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

    internal interface QuizService {
        fun getQuizzes(handler: QuizResponseHandler)

        fun getQuizzes(): Single<List<Quiz>>

        fun getOwned(context: Context, handler: QuizResponseHandler)

        fun getOwned(context: Context): Single<List<Quiz>>

        fun getQuizByID(id: String, handler: QuizResponseHandler)

        fun getQuizByID(id: String): Single<Quiz>

        fun createQuiz(context: Context, quiz: Quiz, handler: QuizResponseHandler)

        fun editQuiz(context: Context, id: String, quiz: Quiz, handler: QuizResponseHandler)

        fun deleteQuiz(context: Context, quiz: Quiz, handler: QuizResponseHandler)

        fun deleteQuiz(context: Context, quiz: Quiz): Completable
    }

    abstract class QuizResponseHandler : Repository.ResponseHandler<ErrorCodes, Quiz>() {
        override fun handle(quiz: Quiz?) {
            //By default, do nothing
        }

        override fun handleMultiple(quizzes: List<Quiz>) {
            //By default, do nothing
        }

        override fun handleErrors(vararg errors: ErrorCodes) {
            //By default, print error codes
            for (e in errors) {
                Log.w(TAG, "Ignored error code: " + e.name)
            }
        }
    }

}
