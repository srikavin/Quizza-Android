package me.srikavin.quiz.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public InternetRepository() {
        retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.7:4000/api/v1/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    protected abstract E mapIntegerErrorCode(int error);

    protected abstract void forwardNetworkError(R handler);

    protected boolean handleAPIErrors(Response<?> response, R handler) {
        try {
            if (!response.isSuccessful() && response.errorBody() != null) {
                JsonObject root = jsonParser.parse(response.errorBody().string()).getAsJsonObject();

                APIError[] errors;
                if (root.has("errors") && root.get("errors").isJsonArray()) {
                    errors = gson.fromJson(root.getAsJsonArray("errors"), APIError[].class);
                } else {
                    return true;
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
                    return true;
                }

                //noinspection unchecked
                E[] tmp = (E[]) Array.newInstance(mapIntegerErrorCode(0).getClass(), 0);
                handler.handleErrors(errs.toArray(tmp));
                return false;
            }

        } catch (Throwable e) {
            e.printStackTrace();
            forwardNetworkError(handler);
            return false;
        }

        //noinspection unchecked
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
