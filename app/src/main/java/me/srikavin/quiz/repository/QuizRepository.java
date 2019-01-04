package me.srikavin.quiz.repository;

import android.content.Context;
import android.util.Log;

import java.util.List;

import androidx.annotation.NonNull;
import me.srikavin.quiz.model.Quiz;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

import static me.srikavin.quiz.MainActivity.TAG;

public enum QuizRepository {
    INSTANCE;

    private InternetQuizRepository internetQuizRepository = new InternetQuizRepository();

    public void getQuizByID(String id, QuizResponseHandler handler) {
        internetQuizRepository.getQuizByID(id, handler);
    }

    public void getQuizzes(QuizResponseHandler handler) {
        internetQuizRepository.getQuizzes(handler);
    }

    public void getOwned(Context context, QuizResponseHandler handler) {
        internetQuizRepository.getOwned(context, handler);
    }

    public void createQuiz(Quiz quiz, QuizResponseHandler handler) {
        internetQuizRepository.createQuiz(quiz, handler);
    }

    public void editQuiz(String id, Quiz quiz, QuizResponseHandler handler) {
        internetQuizRepository.editQuiz(id, quiz, handler);
    }

    public void deleteQuiz(Quiz quiz, QuizResponseHandler handler) {
        internetQuizRepository.deleteQuiz(quiz, handler);
    }

    public enum ErrorCodes {
        UNKNOWN_ERROR(0),
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

    interface QuizService {
        void getQuizzes(QuizResponseHandler handler);

        void getOwned(Context context, QuizResponseHandler handler);

        void getQuizByID(String id, QuizResponseHandler handler);

        void createQuiz(Quiz quiz, QuizResponseHandler handler);

        void editQuiz(String id, Quiz quiz, QuizResponseHandler handler);

        void deleteQuiz(Quiz quiz, QuizResponseHandler handler);
    }

    public abstract static class QuizResponseHandler extends Repository.ResponseHandler<ErrorCodes, Quiz> {
        public void handle(Quiz quiz) {
            //By default, do nothing
        }

        public void handleMultiple(List<Quiz> quizzes) {
            //By default, do nothing
        }

        @Override
        public void handleErrors(@NonNull ErrorCodes... errors) {
            //By default, print error codes
            for (QuizRepository.ErrorCodes e : errors) {
                Log.w(TAG, "Ignored error code: " + e.name());
            }
        }
    }

    static class InternetQuizRepository extends InternetRepository<Quiz, ErrorCodes, Repository.ResponseHandler<ErrorCodes, Quiz>> implements QuizService {

        private final InternetQuizService quizService;

        @Override
        protected ErrorCodes mapIntegerErrorCode(int error) {
            return ErrorCodes.fromCode(error);
        }

        @Override
        protected void forwardNetworkError(ResponseHandler<ErrorCodes, Quiz> handler) {
            handler.handleErrors(new ErrorCodes[]{ErrorCodes.UNKNOWN_ERROR});
        }

        InternetQuizRepository() {
            quizService = retrofit.create(InternetQuizService.class);
        }

        @Override
        public void getQuizzes(final QuizResponseHandler handler) {
            quizService.getQuizzes().enqueue(new DefaultMultiRetrofitCallbackHandler(handler));
        }

        @Override
        public void getOwned(Context context, QuizResponseHandler handler) {
            ensureAuthorized(context);
            quizService.getOwned().enqueue(new DefaultMultiRetrofitCallbackHandler(handler));
        }

        @Override
        public void getQuizByID(String id, final QuizResponseHandler handler) {
            quizService.getQuizByID(id).enqueue(new DefaultRetrofitCallbackHandler(handler));
        }

        @Override
        public void createQuiz(Quiz quiz, final QuizResponseHandler handler) {
            quizService.createQuiz(quiz).enqueue(new DefaultRetrofitCallbackHandler(handler));
        }

        @Override
        public void editQuiz(String id, Quiz quiz, QuizResponseHandler handler) {
            quizService.editQuiz(id, quiz).enqueue(new DefaultRetrofitCallbackHandler(handler));
        }

        @Override
        public void deleteQuiz(Quiz quiz, final QuizResponseHandler handler) {
            quizService.deleteQuiz(quiz.id).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    handler.handle(null);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    forwardNetworkError(handler);
                }
            });
        }

        interface InternetQuizService {
            @GET("quizzes/")
            Call<List<Quiz>> getQuizzes();

            @GET("quizzes/owned")
            Call<List<Quiz>> getOwned();

            @GET("quizzes/{id}")
            Call<Quiz> getQuizByID(@Path("id") String id);

            @POST("quizzes/")
            Call<Quiz> createQuiz(@Body Quiz quiz);

            @DELETE("quizzes/{id}")
            Call<ResponseBody> deleteQuiz(@Path("id") String id);

            @PUT("quizzes/{id}")
            Call<Quiz> editQuiz(@Path("id") String id, @Body Quiz quiz);
        }
    }

    public static class LocalQuizRepository {
    }
}
