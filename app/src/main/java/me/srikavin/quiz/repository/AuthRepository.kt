package me.srikavin.quiz.repository

import android.content.Context
import android.util.Log
import com.google.gson.annotations.Expose
import me.srikavin.quiz.model.AuthUser
import me.srikavin.quiz.repository.internet.InternetRepository
import me.srikavin.quiz.view.main.TAG
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

object AuthRepository {

    private val internetAuthRepository = InternetAuthRepository()
    private val localAuthRepository = LocalAuthRepository()

    fun register(username: String, password: String, handler: AuthResponseHandler) {
        internetAuthRepository.register(username, password, handler)
    }

    fun loginOauthGoogle(token: String, handler: AuthResponseHandler) {
        internetAuthRepository.loginOauthGoogle(token, handler)
    }

    fun login(username: String, password: String, handler: AuthResponseHandler) {
        internetAuthRepository.login(username, password, handler)
    }

    fun setAuthToken(context: Context, token: String?) {
        localAuthRepository.setAuthToken(context, token)
    }

    fun getAuthToken(context: Context): String? {
        return localAuthRepository.getAuthToken(context)
    }

    fun verifyAuth(context: Context, handler: AuthResponseHandler) {
        internetAuthRepository.verifyAuth(context, handler)
    }

    fun logout(context: Context) {
        setAuthToken(context, null)
    }

    fun getUser(): AuthUser? {
        return internetAuthRepository.getUser()
    }

    enum class ErrorCodes(private val code: Int) {
        UNKNOWN_ERROR(0),
        USERNAME_OR_PASSWORD_INCORRECT(1),
        USERNAME_INVALID(2),
        USERNAME_TAKEN(3),
        PASSWORD_INVALID(4),
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

    internal interface AuthService {
        fun register(username: String, password: String, handler: AuthResponseHandler)

        fun login(username: String, password: String, handler: AuthResponseHandler)

        fun loginOauthGoogle(token: String, handler: AuthResponseHandler)

        fun verifyAuth(context: Context, handler: AuthResponseHandler)

        fun getUser(): AuthUser?
    }

    abstract class AuthResponseHandler : Repository.ResponseHandler<ErrorCodes, AuthUser>() {
        override fun handle(user: AuthUser?) {
            //By default, do nothing
        }

        override fun handleMultiple(users: List<AuthUser>) {
            //By default, do nothing
        }

        override fun handleErrors(vararg errors: ErrorCodes) {
            //By default, print error codes
            for (e in errors) {
                Log.w(TAG, "Ignored error code: " + e.name)
            }
        }

        open fun handleVerify(result: Boolean) {
            //By default, do nothing
        }
    }

    internal class InternetAuthRepository : InternetRepository<AuthUser, ErrorCodes, AuthResponseHandler>(), AuthService {
        private val userService: InternetUserService

        private var currentUser: AuthUser? = null

        init {
            userService = retrofit.create(InternetUserService::class.java)
        }

        override fun mapIntegerErrorCode(error: Int): ErrorCodes {
            return ErrorCodes.fromCode(error)
        }

        override fun forwardNetworkError(handler: AuthResponseHandler) {
            handler.handleErrors(ErrorCodes.NETWORK_ERROR)
        }

        override fun getUser(): AuthUser? {
            return currentUser
        }

        override fun register(username: String, password: String, handler: AuthResponseHandler) {
            userService
                    .register(LoginInformation(username, password))
                    .enqueue(object : DefaultRetrofitCallbackHandler(handler) {
                        override fun handle(data: AuthUser?) {
                            super.handle(data)
                            if (data != null) {
                                currentUser = data
                            }
                        }
                    })
        }

        override fun login(username: String, password: String, handler: AuthResponseHandler) {
            userService
                    .login(LoginInformation(username, password))
                    .enqueue(object : DefaultRetrofitCallbackHandler(handler) {
                        override fun handle(data: AuthUser?) {
                            super.handle(data)
                            if (data != null) {
                                currentUser = data
                            }
                        }
                    })
        }

        override fun verifyAuth(context: Context, handler: AuthResponseHandler) {
            ensureAuthorized(context)
            userService.verifyAuth().enqueue(object : Callback<AuthUser> {
                override fun onResponse(call: Call<AuthUser>, response: Response<AuthUser>) {
                    if (response.body() != null && response.body()!!.isAuth) {
                        handler.handleVerify(true)
                    }
                }

                override fun onFailure(call: Call<AuthUser>, t: Throwable) {
                    handler.handleVerify(false)
                }
            })
        }

        override fun loginOauthGoogle(token: String, handler: AuthResponseHandler) {
            userService
                    .loginOauthGoogle(GoogleOauthToken(token))
                    .enqueue(object : DefaultRetrofitCallbackHandler(handler) {
                        override fun handle(data: AuthUser?) {
                            super.handle(data)
                            if (data != null) {
                                currentUser = data
                            }
                        }
                    })
        }

        internal interface InternetUserService {
            @POST("auth/register")
            fun register(@Body loginInformation: LoginInformation): Call<AuthUser>

            @POST("auth/login")
            fun login(@Body loginInformation: LoginInformation): Call<AuthUser>

            @POST("auth/google")
            fun loginOauthGoogle(@Body token: GoogleOauthToken): Call<AuthUser>

            @GET("auth/me")
            fun verifyAuth(): Call<AuthUser>
        }

        internal class GoogleOauthToken(@Expose val key: String)

        internal class LoginInformation(@Expose
                                        var username: String, @Expose
                                        var password: String)
    }

    class LocalAuthRepository {
        fun getAuthToken(context: Context): String? {
            return context.getSharedPreferences("me.srikavin.quiz", Context.MODE_PRIVATE).getString("auth_token", null)
        }

        fun setAuthToken(context: Context, token: String?) {
            context.getSharedPreferences("me.srikavin.quiz", Context.MODE_PRIVATE).edit().putString("auth_token", token).apply()
        }
    }
}
