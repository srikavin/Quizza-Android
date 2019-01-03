package me.srikavin.quiz.repository;

import android.util.Log;

import com.google.gson.annotations.Expose;

import java.util.List;

import androidx.annotation.Nullable;
import me.srikavin.quiz.model.AuthUser;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

import static me.srikavin.quiz.MainActivity.TAG;

public enum AuthRepository {
    INSTANCE;

    private InternetUserRepository internetUserRepository = new InternetUserRepository();

    public void register(String username, String password, AuthResponseHandler handler) {
        internetUserRepository.register(username, password, handler);
    }

    public void login(String username, String password, AuthResponseHandler handler) {
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
        void getUserByID(String id, AuthResponseHandler handler);

        void register(String username, String password, AuthResponseHandler handler);

        void login(String username, String password, AuthResponseHandler handler);
    }

    public abstract static class AuthResponseHandler extends Repository.ResponseHandler<ErrorCodes, AuthUser> {
        public void handle(@Nullable AuthUser user) {
            //By default, do nothing
        }

        public void handleMultiple(@Nullable List<AuthUser> users) {
            //By default, do nothing
        }

        public void handleErrors(ErrorCodes... errors) {
            //By default, print error codes
            for (ErrorCodes e : errors) {
                Log.w(TAG, "Ignored error code: " + e.name());
            }
        }
    }

    static class InternetUserRepository extends InternetRepository<AuthUser, ErrorCodes, InternetRepository.ResponseHandler<ErrorCodes, AuthUser>> implements UserService {

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
        public void getUserByID(String id, AuthResponseHandler handler) {

        }

        @Override
        public void register(String username, String password, final AuthResponseHandler handler) {
            userService
                    .register(new LoginInformation(username, password))
                    .enqueue(new DefaultRetrofitCallbackHandler(handler));
        }

        @Override
        public void login(String username, String password, final AuthResponseHandler handler) {
            userService.login(new LoginInformation(username, password)).enqueue(new Callback<AuthUser>() {
                @Override
                public void onResponse(Call<AuthUser> call, Response<AuthUser> response) {
                    handler.handle(response.body());
                }

                @Override
                public void onFailure(Call<AuthUser> call, Throwable t) {
                    handler.handleErrors(ErrorCodes.NETWORK_ERROR);
                }
            });
        }

        interface InternetUserService {
            @GET("users/{id}")
            Call<AuthUser> getUserByID(@Path("id") String id);

            @POST("auth/register")
            Call<AuthUser> register(@Body LoginInformation loginInformation);

            @POST("auth/login")
            Call<AuthUser> login(@Body LoginInformation loginInformation);
        }

        static class LoginInformation {
            @Expose
            String username;
            @Expose
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
