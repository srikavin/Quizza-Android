package me.srikavin.quiz.repository.internet

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.annotations.Expose
import me.srikavin.quiz.repository.AuthRepository
import me.srikavin.quiz.repository.Repository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.standalone.KoinComponent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

/**
 * @param <E> The enum containing possible error codes returned from the api
</E> */
internal abstract class InternetRepository<T, E : Enum<*>, R : Repository.ResponseHandler<E, T>> : Repository(), KoinComponent {
    private var authRepository: AuthRepository = AuthRepository
    protected val retrofit: Retrofit
    protected val gson: Gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
    private val jsonParser = JsonParser()
    private val interceptor: AuthRequestInterceptor
    private val statusRepository: Status

    init {
        interceptor = AuthRequestInterceptor()
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        retrofit = Retrofit.Builder()
                .baseUrl("https://quiz.srikavin.me/api/v1/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build()
        statusRepository = retrofit.create(Status::class.java)
    }

    protected fun ensureAuthorized(context: Context) {
        val token = authRepository.getAuthToken(context)
        interceptor.setToken(token)
    }

    protected fun checkStatus() {
        statusRepository.status.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                offlineMode = false
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                offlineMode = true
            }
        })
    }

    /**
     * Processes the response for error codes, calling [InternetRepository.mapIntegerErrorCode],
     * to map error codes, returning an array of errors, or null if no errors are present.
     *
     * @param response The Retrofit [Response] object received from an [Call.enqueue]
     * method call.
     * @return An array containing errors or null if no errors are present.
     */
    @Throws(IOException::class)
    protected fun getAPIErrors(response: Response<*>): kotlin.Array<E>? {
        if (!response.isSuccessful && response.errorBody() != null) {
            println(response.errorBody()!!.string())
            val root = jsonParser.parse(response.errorBody()!!.string()).asJsonObject

            val errors: kotlin.Array<APIError>
            if (root.has("errors") && root.get("errors").isJsonArray) {
                errors = gson.fromJson<kotlin.Array<APIError>>(root.getAsJsonArray("errors"), kotlin.Array<APIError>::class.java)
            } else {
                return null
            }

            val errs = HashSet<E>(errors.size)
            for (error in errors) {
                var mapped: E? = mapIntegerErrorCode(error.code)
                if (mapped == null) {
                    mapped = mapIntegerErrorCode(0)
                }
                errs.add(mapped)
            }

            if (errs.size == 0) {
                return null
            }

            @Suppress("UNCHECKED_CAST")
            return errs.toArray() as kotlin.Array<E>?
        }
        return null
    }

    internal class AuthRequestInterceptor : Interceptor {
        private var token = ""

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val ongoing = chain.request().newBuilder()
            ongoing.addHeader("x-access-token", this.token)
            return chain.proceed(ongoing.build())
        }

        fun setToken(token: String?) {
            if (token != null) {
                this.token = token
            }
        }
    }

    protected abstract fun mapIntegerErrorCode(error: Int): E

    protected abstract fun forwardNetworkError(handler: R)

    private interface Status {
        @get:GET("status")
        val status: Call<Void>
    }

    protected fun handleAPIErrors(response: Response<*>, handler: R): Boolean {
        try {
            val errors = getAPIErrors(response) ?: return true

            handler.handleErrors(*errors)
        } catch (e: Throwable) {
            e.printStackTrace()
            forwardNetworkError(handler)
            return false
        }

        return true
    }

    class APIError {
        @Expose
        var msg: String? = null
        @Expose
        var code: Int = 0
    }

    protected abstract inner class RetrofitCallbackHandler<W>(val handler: R) : Callback<W> {

        override fun onResponse(call: Call<W>, response: Response<W>) {
            if (handleAPIErrors(response, handler)) {
                handle(response.body())
            }
        }

        override fun onFailure(call: Call<W>, t: Throwable) {
            t.printStackTrace()
            forwardNetworkError(handler)

        }

        abstract fun handle(data: W?)
    }

    protected open inner class DefaultRetrofitCallbackHandler(handler: R) : RetrofitCallbackHandler<T>(handler) {

        override fun handle(data: T?) {
            handler.handle(data)
        }
    }

    protected inner class DefaultMultiRetrofitCallbackHandler(handler: R) : RetrofitCallbackHandler<List<T>>(handler) {

        override fun handle(data: List<T>?) {
            handler.handleMultiple(data ?: ArrayList())
        }
    }

    companion object {
        protected var offlineMode = false
    }
}
