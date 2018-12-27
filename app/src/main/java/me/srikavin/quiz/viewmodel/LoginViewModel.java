package me.srikavin.quiz.viewmodel;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import me.srikavin.quiz.model.UserProfile;
import me.srikavin.quiz.repository.UserRepository;

import static me.srikavin.quiz.MainActivity.TAG;

public class LoginViewModel extends ViewModel {
    private MutableLiveData<ErrorWrapper<UserProfile>> currentUser;

    public LiveData<ErrorWrapper<UserProfile>> register(String username, String password) {
        if (currentUser == null) {
            currentUser = new MutableLiveData<>();
        }
        registerAccount(username, password);
        return currentUser;
    }

    public LiveData<ErrorWrapper<UserProfile>> login(String username, String password) {
        if (currentUser == null) {
            currentUser = new MutableLiveData<>();
        }
        loginAccount(username, password);
        return currentUser;
    }

    private void loginAccount(String username, String password) {
        UserRepository.INSTANCE.login(username, password, new UserRepository.UserResponseHandler() {
            @Override
            public void handle(@Nullable UserProfile user) {
                ErrorWrapper<UserProfile> ret = new ErrorWrapper<>(user, null);
                currentUser.postValue(ret);
            }

            @Override
            public void handleErrors(UserRepository.ErrorCodes... errors) {
                ErrorWrapper<UserProfile> ret = new ErrorWrapper<>(null, null);
                currentUser.postValue(ret);
            }
        });
    }

    public void registerAccount(String username, String password) {
        UserRepository.INSTANCE.register(username, password, new UserRepository.UserResponseHandler() {
            @Override
            public void handle(@Nullable UserProfile user) {
                ErrorWrapper<UserProfile> ret = new ErrorWrapper<>(user, null);
                currentUser.postValue(ret);
            }

            @Override
            public void handleErrors(UserRepository.ErrorCodes... errors) {
                for (UserRepository.ErrorCodes e : errors) {
                    Log.w(TAG, "Ignored error code: " + e.name());
                }


                ErrorWrapper<UserProfile> ret = new ErrorWrapper<>(null, null);
                currentUser.postValue(ret);
            }
        });

    }

    static class ErrorState {
        private int error;
        private String errorMessage;

        public ErrorState(int error, String errorMessage) {
            this.error = error;
            this.errorMessage = errorMessage;
        }
    }

    static class ErrorWrapper<T> {
        T data;
        ErrorState error;

        public ErrorWrapper(T data, ErrorState error) {
            this.data = data;
            this.error = error;
        }

        public T getLiveData() {
            return data;
        }

        public ErrorState getError() {
            return error;
        }
    }
}
