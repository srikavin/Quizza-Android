package me.srikavin.quiz.viewmodel;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import me.srikavin.quiz.model.AuthUser;
import me.srikavin.quiz.repository.AuthRepository;
import me.srikavin.quiz.repository.error.ErrorWrapper;

import static me.srikavin.quiz.MainActivity.TAG;

public class LoginViewModel extends ViewModel {
    private MutableLiveData<ErrorWrapper<AuthUser, AuthRepository.ErrorCodes>> currentUser;

    public LiveData<ErrorWrapper<AuthUser, AuthRepository.ErrorCodes>> register(String username, String password) {
        if (currentUser == null) {
            currentUser = new MutableLiveData<>();
        }
        registerAccount(username, password);
        return currentUser;
    }

    public LiveData<ErrorWrapper<AuthUser, AuthRepository.ErrorCodes>> login(String username, String password) {
        if (currentUser == null) {
            currentUser = new MutableLiveData<>();
        }
        loginAccount(username, password);
        return currentUser;
    }

    private void loginAccount(String username, String password) {
        AuthRepository.INSTANCE.login(username, password, new AuthRepository.AuthResponseHandler() {
            @Override
            public void handle(@Nullable AuthUser user) {
                ErrorWrapper<AuthUser, AuthRepository.ErrorCodes> ret = new ErrorWrapper<>(user, null);
                currentUser.postValue(ret);
            }

            @Override
            public void handleErrors(AuthRepository.ErrorCodes... errors) {
                ErrorWrapper<AuthUser, AuthRepository.ErrorCodes> ret = new ErrorWrapper<>(null, errors);
                currentUser.postValue(ret);
            }
        });
    }

    public void registerAccount(String username, String password) {
        AuthRepository.INSTANCE.register(username, password, new AuthRepository.AuthResponseHandler() {
            @Override
            public void handle(@Nullable AuthUser user) {
                ErrorWrapper<AuthUser, AuthRepository.ErrorCodes> ret = new ErrorWrapper<>(user, null);
                currentUser.postValue(ret);
            }

            @Override
            public void handleErrors(AuthRepository.ErrorCodes... errors) {
                for (AuthRepository.ErrorCodes e : errors) {
                    Log.w(TAG, "Ignored error code: " + e.name());
                }

                ErrorWrapper<AuthUser, AuthRepository.ErrorCodes> ret = new ErrorWrapper<>(null, errors);
                currentUser.postValue(ret);
            }
        });

    }
}
