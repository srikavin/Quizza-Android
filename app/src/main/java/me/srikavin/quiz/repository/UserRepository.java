package me.srikavin.quiz.repository;

import android.util.Log;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import me.srikavin.quiz.model.UserProfile;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

import static me.srikavin.quiz.MainActivity.TAG;

public enum UserRepository {
    INSTANCE;

    private InternetUserRepository internetUserRepository = new InternetUserRepository();

    public void getUserByID(String id, UserResponseHandler handler) {
        internetUserRepository.getUserByID(id, handler);
    }

    public void register(String username, String password, UserResponseHandler handler) {
        internetUserRepository.register(username, password, handler);
    }

    public void login(String username, String password, UserResponseHandler handler) {
        internetUserRepository.login(username, password, handler);
    }


    public enum ErrorCodes {
        UNKNOWN_ERROR(0),
        USERNAME_OR_PASSWORD_INCORRECT(1),
        USERNAME_INVALID(2),
        USERNAME_TAKEN(3),
        PASSWORD_INVALID(4),
        NETWORK_ERROR(5),
        SERVER_ERROR(6);

        private int code;

        ErrorCodes(int code) {
            this.code = code;
        }

        static ErrorCodes fromCode(int errorCode) {
            for (ErrorCodes e : values()) {
                if (errorCode == e.code) {
                    return e;
                }
            }
            return UNKNOWN_ERROR;
        }
    }

    interface UserService {
        void getUserByID(String id, UserResponseHandler handler);

        void register(String username, String password, UserResponseHandler handler);

        void login(String username, String password, UserResponseHandler handler);
    }

    public abstract static class UserResponseHandler extends Repository.ResponseHandler<ErrorCodes, UserProfile> {
        public void handle(@Nullable UserProfile user) {
            //By default, do nothing
        }

        @WorkerThread
        public void handleMultiple(@Nullable List<UserProfile> users) {
            //By default, do nothing
        }

        @WorkerThread
        public void handleErrors(ErrorCodes... errors) {
            //By default, print error codes
            for (ErrorCodes e : errors) {
                Log.w(TAG, "Ignored error code: " + e.name());
            }
        }
    }

    static class InternetUserRepository extends InternetRepository<UserProfile, ErrorCodes, InternetRepository.ResponseHandler<ErrorCodes, UserProfile>> implements UserService {

        private final InternetUserService userService;

        InternetUserRepository() {
            userService = retrofit.create(InternetUserService.class);
        }

        @Override
        protected ErrorCodes mapIntegerErrorCode(int error) {
            return ErrorCodes.fromCode(error);
        }

        @Override
        protected void forwardNetworkError(ResponseHandler handler) {
            handler.handleErrors(new ErrorCodes[]{ErrorCodes.NETWORK_ERROR});
        }

        @Override
        public void getUserByID(String id, UserResponseHandler handler) {

        }

        @Override
        public void register(String username, String password, final UserResponseHandler handler) {
            userService
                    .register(new LoginInformation(username, password))
                    .enqueue(new DefaultRetrofitCallbackHandler(handler));
        }

        @Override
        public void login(String username, String password, final UserResponseHandler handler) {
            userService.login(new LoginInformation(username, password)).enqueue(new Callback<UserProfile>() {
                @Override
                public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                    handler.handle(response.body());
                }

                @Override
                public void onFailure(Call<UserProfile> call, Throwable t) {
                    handler.handleErrors(ErrorCodes.NETWORK_ERROR);
                }
            });
        }

        interface InternetUserService {
            @GET("users/{id}")
            Call<UserProfile> getUserByID(@Path("id") String id);

            @POST("auth/register")
            Call<UserProfile> register(@Body LoginInformation loginInformation);

            @POST("auth/login")
            Call<UserProfile> login(@Body LoginInformation loginInformation);
        }

        static class LoginInformation {
            String username;
            String password;

            public LoginInformation(String username, String password) {
                this.username = username;
                this.password = password;
            }
        }
    }

    public static class LocalUserRepository {
    }
}
