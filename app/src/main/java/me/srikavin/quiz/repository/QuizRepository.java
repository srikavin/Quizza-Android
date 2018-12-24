package me.srikavin.quiz.repository;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import me.srikavin.quiz.model.Quiz;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;

public enum QuizRepository {
    INSTANCE;

    private InternetQuizRepository internetQuizRepository = new InternetQuizRepository();

    public void getQuizByID(String id, QuizResponseHandler handler) {
        internetQuizRepository.getQuizByID(id, handler);
    }

    public void getQuizzes(QuizResponseHandler handler) {
        internetQuizRepository.getQuizzes(handler);
    }

    interface QuizService {
        void getQuizzes(QuizResponseHandler handler);

        void getQuizByID(String id, QuizResponseHandler handler);

    }

    public abstract static class QuizResponseHandler {
        @WorkerThread
        public void handleQuiz(@Nullable Quiz quizzes) {
            //By default, do nothing
        }

        @WorkerThread
        public void handleQuizzes(@Nullable List<Quiz> quizzes) {
            //By default, do nothing
        }
    }

    static class InternetQuizRepository extends InternetRepository implements QuizService {

        private final InternetQuizService quizService;

        InternetQuizRepository() {
            quizService = retrofit.create(InternetQuizService.class);
        }

        @Override
        public void getQuizzes(final QuizResponseHandler handler) {
            quizService.getQuizzes().enqueue(new Callback<List<Quiz>>() {
                @Override
                public void onResponse(Call<List<Quiz>> call, Response<List<Quiz>> response) {
                    handler.handleQuizzes(response.body());
                }

                public void onFailure(Call<List<Quiz>> call, Throwable t) {
                    handler.handleQuizzes(null);
                }
            });
        }

        @Override
        public void getQuizByID(String id, final QuizResponseHandler handler) {
            quizService.getQuizByID(id).enqueue(new Callback<Quiz>() {
                @Override
                public void onResponse(Call<Quiz> call, Response<Quiz> response) {
                    handler.handleQuiz(response.body());
                }

                @Override
                public void onFailure(Call<Quiz> call, Throwable t) {
                    handler.handleQuiz(null);
                }
            });
        }

        interface InternetQuizService {
            @GET("quizzes/")
            Call<List<Quiz>> getQuizzes();

            @GET("quizzes/{id}")
            Call<Quiz> getQuizByID(@Path("id") String id);
        }

        private class QuizService {

        }
    }

    public static class LocalQuizRepository {
    }
}
