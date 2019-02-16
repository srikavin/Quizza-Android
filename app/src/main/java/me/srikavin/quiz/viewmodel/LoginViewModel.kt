package me.srikavin.quiz.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import me.srikavin.quiz.model.AuthUser
import me.srikavin.quiz.repository.AuthRepository
import me.srikavin.quiz.repository.error.ErrorWrapper
import me.srikavin.quiz.view.main.TAG
import org.koin.standalone.KoinComponent

class LoginViewModel(application: Application) : AndroidViewModel(application), KoinComponent {
    private var currentUser: MutableLiveData<ErrorWrapper<AuthUser, AuthRepository.ErrorCodes>> = MutableLiveData()
    val authUser: LiveData<ErrorWrapper<AuthUser, AuthRepository.ErrorCodes>>
        get() {
            return currentUser
        }

    fun register(username: String, password: String) {
        registerAccount(username, password)
    }

    fun login(username: String, password: String) {
        loginAccount(username, password)
    }

    fun loginGoogleAuth(token: String) {
        AuthRepository.loginOauthGoogle(token, object : AuthRepository.AuthResponseHandler() {
            override fun handle(user: AuthUser?) {
                AuthRepository.setAuthToken(getApplication<Application>().applicationContext, user!!.token!!)
                val ret = ErrorWrapper<AuthUser, AuthRepository.ErrorCodes>(user, null)
                currentUser.postValue(ret)
            }

            override fun handleErrors(vararg errors: AuthRepository.ErrorCodes) {
                val ret = ErrorWrapper<AuthUser, AuthRepository.ErrorCodes>(null, errors)
                currentUser.postValue(ret)
            }
        })
    }


    fun verifyAuth(): LiveData<Boolean> {
        val ret = MutableLiveData<Boolean>()
        AuthRepository.verifyAuth(getApplication(), object : AuthRepository.AuthResponseHandler() {
            override fun handleVerify(result: Boolean) {
                ret.postValue(result)
            }
        })
        return ret
    }

    private fun loginAccount(username: String, password: String) {
        AuthRepository.login(username, password, object : AuthRepository.AuthResponseHandler() {
            override fun handle(user: AuthUser?) {
                AuthRepository.setAuthToken(getApplication<Application>().applicationContext, user!!.token!!)
                val ret = ErrorWrapper<AuthUser, AuthRepository.ErrorCodes>(user, null)
                currentUser.postValue(ret)
            }

            override fun handleErrors(vararg errors: AuthRepository.ErrorCodes) {
                val ret = ErrorWrapper<AuthUser, AuthRepository.ErrorCodes>(null, errors)
                currentUser.postValue(ret)
            }
        })
    }

    private fun registerAccount(username: String, password: String) {
        AuthRepository.register(username, password, object : AuthRepository.AuthResponseHandler() {
            override fun handle(user: AuthUser?) {
                AuthRepository.setAuthToken(getApplication<Application>().applicationContext, user!!.token!!)
                val ret = ErrorWrapper<AuthUser, AuthRepository.ErrorCodes>(user, null)
                currentUser.postValue(ret)
            }

            override fun handleErrors(vararg errors: AuthRepository.ErrorCodes) {
                for (e in errors) {
                    Log.w(TAG, "Ignored error code: " + e.name)
                }

                val ret = ErrorWrapper<AuthUser, AuthRepository.ErrorCodes>(null, errors)
                currentUser.postValue(ret)
            }
        })

    }
}
