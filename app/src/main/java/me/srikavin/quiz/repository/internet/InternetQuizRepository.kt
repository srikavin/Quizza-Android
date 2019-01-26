package me.srikavin.quiz.repository.internet

import android.content.Context
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import me.srikavin.quiz.model.Quiz
import me.srikavin.quiz.repository.QuizRepository
import me.srikavin.quiz.repository.Repository
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.*

internal class InternetQuizRepository : InternetRepository<Quiz, QuizRepository.ErrorCodes, Repository.ResponseHandler<QuizRepository.ErrorCodes, Quiz>>(), QuizRepository.QuizService {
    private val quizService: InternetQuizService

    override fun mapIntegerErrorCode(error: Int): QuizRepository.ErrorCodes {
        return QuizRepository.ErrorCodes.fromCode(error)
    }

    override fun forwardNetworkError(handler: Repository.ResponseHandler<QuizRepository.ErrorCodes, Quiz>) {
        handler.handleErrors(QuizRepository.ErrorCodes.UNKNOWN_ERROR)
    }

    init {
        quizService = retrofit.create(InternetQuizService::class.java)
    }

    override fun getQuizzes(handler: QuizRepository.QuizResponseHandler) {
        quizService.getQuizzes().enqueue(DefaultMultiRetrofitCallbackHandler(handler))
    }

    override fun getQuizzes(): Single<List<Quiz>> {
        return quizService.getQuizzesn().subscribeOn(Schedulers.io())
    }

    override fun getOwned(context: Context, handler: QuizRepository.QuizResponseHandler) {
        ensureAuthorized(context)
        quizService.getOwned().enqueue(DefaultMultiRetrofitCallbackHandler(handler))
    }


    override fun getQuizByID(id: String, handler: QuizRepository.QuizResponseHandler) {
        quizService.getQuizByID(id).enqueue(DefaultRetrofitCallbackHandler(handler))
    }

    override fun getQuizByID(id: String): Single<Quiz> {
        return quizService.getQuizByIDn(id).subscribeOn(Schedulers.io())
    }

    override fun createQuiz(context: Context, quiz: Quiz, handler: QuizRepository.QuizResponseHandler) {
        ensureAuthorized(context)
        quizService.createQuiz(quiz).enqueue(DefaultRetrofitCallbackHandler(handler))
    }

    override fun editQuiz(context: Context, id: String, quiz: Quiz, handler: QuizRepository.QuizResponseHandler) {
        ensureAuthorized(context)
        quizService.editQuiz(id, quiz).enqueue(DefaultRetrofitCallbackHandler(handler))
    }

    override fun deleteQuiz(context: Context, quiz: Quiz, handler: QuizRepository.QuizResponseHandler) {
        ensureAuthorized(context)
        quizService.deleteQuiz(quiz.id).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                handler.handle(null)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                forwardNetworkError(handler)
            }
        })
    }

    internal interface InternetQuizService {
        @GET("quizzes/")
        fun getQuizzes(): Call<List<Quiz>>

        @GET("quizzes/")
        fun getQuizzesn(): Single<List<Quiz>>

        @GET("quizzes/owned")
        fun getOwned(): Call<List<Quiz>>

        @GET("quizzes/{id}")
        fun getQuizByIDn(@Path("id") id: String): Single<Quiz>

        @GET("quizzes/{id}")
        fun getQuizByID(@Path("id") id: String): Call<Quiz>

        @POST("quizzes/")
        fun createQuiz(@Body quiz: Quiz): Call<Quiz>

        @DELETE("quizzes/{id}")
        fun deleteQuiz(@Path("id") id: String): Call<ResponseBody>

        @PUT("quizzes/{id}")
        fun editQuiz(@Path("id") id: String, @Body quiz: Quiz): Call<Quiz>
    }
}