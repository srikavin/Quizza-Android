package me.srikavin.quiz.repository;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.Expose;

import java.util.List;

import androidx.annotation.Nullable;
import me.srikavin.quiz.model.AuthUser;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.POST;

import static me.srikavin.quiz.MainActivity.TAG;

public enum AuthRepository {
    INSTANCE;

    private InternetAuthRepository internetAuthRepository = new InternetAuthRepository();
    private LocalAuthRepository localAuthRepository = new LocalAuthRepository();

    public void register(String username, String password, AuthResponseHandler handler) {
        internetAuthRepository.register(username, password, handler);
    }

    public void login(String username, String password, AuthResponseHandler handler) {
        internetAuthRepository.login(username, password, handler);
    }

    public void setAuthToken(Context context, String token) {
        localAuthRepository.setAuthToken(context, token);
    }

    public String getAuthToken(Context context) {
        return localAuthRepository.getAuthToken(context);
    }

    public boolean verifyAuth() {
        return true;
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

    interface AuthService {
        void register(String username, String password, AuthResponseHandler handler);

        void login(String username, String password, AuthResponseHandler handler);

        void verifyAuth(Context context, AuthResponseHandler handler);
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

        public void handleVerify(boolean result) {
            //By default, do nothing
        }
    }

    static class InternetAuthRepository extends InternetRepository<AuthUser, ErrorCodes, InternetRepository.ResponseHandler<ErrorCodes, AuthUser>> implements AuthService {

        private final InternetUserService userService;

        InternetAuthRepository() {
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
        public void register(String username, String password, final AuthResponseHandler handler) {
            userService
                    .register(new LoginInformation(username, password))
                    .enqueue(new DefaultRetrofitCallbackHandler(handler));
        }

        @Override
        public void login(String username, String password, final AuthResponseHandler handler) {
            userService
                    .login(new LoginInformation(username, password))
                    .enqueue(new DefaultRetrofitCallbackHandler(handler));
        }

        @Override
        public void verifyAuth(Context context, final AuthResponseHandler handler) {
            ensureAuthorized(context);
            userService.verifyAuth().enqueue(new Callback<AuthUser>() {
                @Override
                public void onResponse(Call<AuthUser> call, Response<AuthUser> response) {
                    handler.handleVerify(true);
                }

                @Override
                public void onFailure(Call<AuthUser> call, Throwable t) {
                    handler.handleVerify(false);
                }
            });
        }

        interface InternetUserService {
            @POST("auth/register")
            Call<AuthUser> register(@Body LoginInformation loginInformation);

            @POST("auth/login")
            Call<AuthUser> login(@Body LoginInformation loginInformation);

            @POST("auth/me")
            Call<AuthUser> verifyAuth();
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

    public static class LocalAuthRepository {
        public String getAuthToken(Context context) {
            return context.getSharedPreferences("me.srikavin.quiz", Context.MODE_PRIVATE).getString("auth_token", null);
        }

        public void setAuthToken(Context context, String token) {
            context.getSharedPreferences("me.srikavin.quiz", Context.MODE_PRIVATE).edit().putString("auth_token", token).apply();
        }
    }
}
