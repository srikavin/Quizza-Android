package me.srikavin.quiz.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import me.srikavin.quiz.model.AuthUser
import me.srikavin.quiz.repository.AuthRepository
import me.srikavin.quiz.repository.error.ErrorWrapper

import me.srikavin.quiz.view.main.MainActivity.TAG

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private var currentUser: MutableLiveData<ErrorWrapper<AuthUser, AuthRepository.ErrorCodes>>? = null

    fun register(username: String, password: String): LiveData<ErrorWrapper<AuthUser, AuthRepository.ErrorCodes>> {
        if (currentUser == null) {
            currentUser = MutableLiveData()
        }
        registerAccount(username, password)
        return currentUser as MutableLiveData<ErrorWrapper<AuthUser, AuthRepository.ErrorCodes>>
    }

    fun login(username: String, password: String): LiveData<ErrorWrapper<AuthUser, AuthRepository.ErrorCodes>> {
        if (currentUser == null) {
            currentUser = MutableLiveData()
        }
        loginAccount(username, password)
        return currentUser!!
    }

    fun verifyAuth(): LiveData<Boolean> {
        val ret = MutableLiveData<Boolean>()
        AuthRepository.INSTANCE.verifyAuth(getApplication(), object : AuthRepository.AuthResponseHandler() {
            override fun handleVerify(result: Boolean) {
                ret.postValue(result)
            }
        })
        return ret
    }

    private fun loginAccount(username: String, password: String) {
        AuthRepository.INSTANCE.login(username, password, object : AuthRepository.AuthResponseHandler() {
            override fun handle(user: AuthUser?) {
                AuthRepository.INSTANCE.setAuthToken(getApplication<Application>().applicationContext, user!!.token)
                val ret = ErrorWrapper<AuthUser, AuthRepository.ErrorCodes>(user, null)
                currentUser!!.postValue(ret)
            }

            override fun handleErrors(vararg errors: AuthRepository.ErrorCodes) {
                val ret = ErrorWrapper<AuthUser, AuthRepository.ErrorCodes>(null, errors)
                currentUser!!.postValue(ret)
            }
        })
    }

    fun registerAccount(username: String, password: String) {
        AuthRepository.INSTANCE.register(username, password, object : AuthRepository.AuthResponseHandler() {
            override fun handle(user: AuthUser?) {
                AuthRepository.INSTANCE.setAuthToken(getApplication<Application>().applicationContext, user!!.token)
                val ret = ErrorWrapper<AuthUser, AuthRepository.ErrorCodes>(user, null)
                currentUser!!.postValue(ret)
            }

            override fun handleErrors(vararg errors: AuthRepository.ErrorCodes) {
                for (e in errors) {
                    Log.w(TAG, "Ignored error code: " + e.name)
                }

                val ret = ErrorWrapper<AuthUser, AuthRepository.ErrorCodes>(null, errors)
                currentUser!!.postValue(ret)
            }
        })

    }
}
