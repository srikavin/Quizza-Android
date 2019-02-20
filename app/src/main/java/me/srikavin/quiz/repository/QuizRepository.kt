package me.srikavin.quiz.repository

import android.content.Context
import android.util.Log
import io.reactivex.Completable
import io.reactivex.Single
import me.srikavin.quiz.model.Quiz
import me.srikavin.quiz.repository.internet.InternetQuizRepository
import me.srikavin.quiz.repository.local.LocalQuizRepository
import me.srikavin.quiz.view.main.TAG

/**
 *  Used to fetch quizzes from local or online sources
 */
class QuizRepository(private val context: Context) {
    private val quizRepository: QuizService = LocalQuizRepository(InternetQuizRepository())

    /**
     * Returns a observable [Single] which will return a [Quiz] or result in an error.
     *
     * @param id The quiz id to retrieve
     */
    fun getQuizByID(id: String): Single<Quiz> {
        return quizRepository.getQuizByID(id)
    }


    /**
     * Fetches a quiz with specified id and calls the callback provided
     *
     * @param id The quiz id to retrieve
     * @param handler The handler to call upon fetching the quiz
     */
    fun getQuizByID(id: String, handler: QuizResponseHandler) {
        return quizRepository.getQuizByID(id, handler)
    }

    /**
     * Fetches all of the quizzes available and returns a observable containing the quizzes
     */
    fun getQuizzes(): Single<List<Quiz>> {
        return quizRepository.getQuizzes()
    }

    /**
     * Fetches all of the quizzes owned by the user and returns a observable containing the quizzes
     */
    fun getOwned(): Single<List<Quiz>> {
        return quizRepository.getOwned(context)
    }

    /**
     * Creates a quiz based on the given quiz
     *
     * @param quiz The quiz to persist
     * @param handler The handler to call after a success or failure
     */
    fun createQuiz(quiz: Quiz, handler: QuizResponseHandler) {
        quizRepository.createQuiz(context, quiz, handler)
    }

    /**
     * Updates a quiz based on the given quiz
     * @param id The id of the quiz to update
     * @param quiz The contents of the quiz to update
     * @param handler The handler to call after a success or failure
     */
    fun editQuiz(id: String, quiz: Quiz, handler: QuizResponseHandler) {
        quizRepository.editQuiz(context, id, quiz, handler)
    }


    /**
     * Deletes the given quiz, returning a completable that may contain an error
     */
    fun deleteQuiz(quiz: Quiz): Completable {
        return quizRepository.deleteQuiz(context, quiz)
    }

    /**
     * Represents all of the possible error codes returned from this repository
     */
    enum class ErrorCodes constructor(private val code: Int) {
        /**
         * Represents the occurrence of an unknown error
         */
        UNKNOWN_ERROR(0),
        /**
         * Represents the occurrence of a network error
         */
        NETWORK_ERROR(5),
        /**
         * Represents the occurrence of a server-side error
         */
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

    /**
     * A service that is able to be utilized abstractly inside of a quiz repository
     */
    interface QuizService {
        /**
         * Fetches all of the quizzes available and returns a observable containing the quizzes
         */
        fun getQuizzes(): Single<List<Quiz>>

        /**
         * Fetches all of the quizzes owned by the user and returns a observable containing the quizzes
         */
        fun getOwned(context: Context): Single<List<Quiz>>

        /**
         * Returns a observable [Single] which will return a [Quiz] or result in an error.
         *
         * @param id The quiz id to retrieve
         */
        fun getQuizByID(id: String, handler: QuizResponseHandler)

        /**
         * Returns a observable [Single] which will return a [Quiz] or result in an error.
         *
         * @param id The quiz id to retrieve
         */
        fun getQuizByID(id: String): Single<Quiz>

        /**
         * Creates a quiz based on the given quiz
         *
         * @param quiz The quiz to persist
         * @param handler The handler to call after a success or failure
         */

        fun createQuiz(context: Context, quiz: Quiz, handler: QuizResponseHandler)

        /**
         * Updates a quiz based on the given quiz
         * @param id The id of the quiz to update
         * @param quiz The contents of the quiz to update
         * @param handler The handler to call after a success or failure
         */
        fun editQuiz(context: Context, id: String, quiz: Quiz, handler: QuizResponseHandler)

        /**
         * Deletes the given quiz, returning a [Completable] that may contain an error
         */
        fun deleteQuiz(context: Context, quiz: Quiz): Completable
    }

    /**
     * Used to handle callbacks for quiz repository events
     */
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
