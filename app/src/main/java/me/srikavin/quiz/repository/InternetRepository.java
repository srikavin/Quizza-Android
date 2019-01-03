package me.srikavin.quiz.repository;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @param <E> The enum containing possible error codes returned from the api
 */
public abstract class InternetRepository<T, E extends Enum, R extends Repository.ResponseHandler<E, T>> extends Repository {
    protected Retrofit retrofit;
    protected Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    protected JsonParser jsonParser = new JsonParser();
    private AuthRequestInterceptor interceptor;

    public InternetRepository() {
        interceptor = new AuthRequestInterceptor();
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
        retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.7:4000/api/v1/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
    }

    protected void ensureAuthorized(Context context) {
        String token = AuthRepository.INSTANCE.getAuthToken(context);
        interceptor.setToken(token);
    }

    static class AuthRequestInterceptor implements Interceptor {
        private String token = "";

        @Override
        @NonNull
        public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
            Request.Builder ongoing = chain.request().newBuilder();
            ongoing.addHeader("x-access-token", this.token);
            return chain.proceed(ongoing.build());
        }

        void setToken(String token) {
            this.token = token;
        }
    }

    protected abstract E mapIntegerErrorCode(int error);

    protected abstract void forwardNetworkError(R handler);

    /**
     * Processes the response for error codes, calling {@link InternetRepository#mapIntegerErrorCode(int)},
     * to map error codes, returning an array of errors, or null if no errors are present.
     *
     * @param response The Retrofit {@link Response} object received from an {@link Call#enqueue(Callback)}
     *                 method call.
     * @return An array containing errors or null if no errors are present.
     */
    protected E[] getAPIErrors(Response<?> response) throws IOException {
        if (!response.isSuccessful() && response.errorBody() != null) {
            JsonObject root = jsonParser.parse(response.errorBody().string()).getAsJsonObject();

            APIError[] errors;
            if (root.has("errors") && root.get("errors").isJsonArray()) {
                errors = gson.fromJson(root.getAsJsonArray("errors"), APIError[].class);
            } else {
                return null;
            }

            Set<E> errs = new HashSet<>(errors.length);
            for (APIError error : errors) {
                E mapped = mapIntegerErrorCode(error.code);
                if (mapped == null) {
                    mapped = mapIntegerErrorCode(0);
                }
                errs.add(mapped);
            }

            if (errs.size() == 0) {
                return null;
            }

            //noinspection unchecked
            E[] tmp = (E[]) Array.newInstance(mapIntegerErrorCode(0).getClass(), 0);
            return errs.toArray(tmp);
        }
        return null;
    }

    protected boolean handleAPIErrors(Response<?> response, R handler) {
        try {
            E[] errors = getAPIErrors(response);
            if (errors == null) {
                return true;
            }

            handler.handleErrors(errors);
        } catch (Throwable e) {
            e.printStackTrace();
            forwardNetworkError(handler);
            return false;
        }

        return true;
    }

    public static class APIError {
        @Expose
        public String msg;
        @Expose
        public int code;
    }

    protected abstract class RetrofitCallbackHandler<W> implements Callback<W> {
        private final R handler;

        public RetrofitCallbackHandler(R handler) {
            this.handler = handler;
        }

        @Override
        public void onResponse(Call<W> call, Response<W> response) {
            if (handleAPIErrors(response, getHandler())) {
                handle(response.body());
            }
        }

        @Override
        public void onFailure(Call<W> call, Throwable t) {
            forwardNetworkError(getHandler());

        }

        public final R getHandler() {
            return handler;
        }

        public abstract void handle(W data);
    }

    protected class DefaultRetrofitCallbackHandler extends RetrofitCallbackHandler<T> {
        public DefaultRetrofitCallbackHandler(R handler) {
            super(handler);
        }

        @Override
        public void handle(T data) {
            getHandler().handle(data);
        }
    }

    protected class DefaultMultiRetrofitCallbackHandler extends RetrofitCallbackHandler<List<T>> {
        public DefaultMultiRetrofitCallbackHandler(R handler) {
            super(handler);
        }

        @Override
        public void handle(List<T> data) {
            getHandler().handleMultiple(data);
        }
    }
}
