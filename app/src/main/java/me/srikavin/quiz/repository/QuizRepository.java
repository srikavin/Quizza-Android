package me.srikavin.quiz.repository;

import java.util.List;
import java.util.UUID;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import me.srikavin.quiz.model.Quiz;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;

public enum QuizRepository {
    INSTANCE;

    private InternetQuizRepository internetQuizRepository = new InternetQuizRepository();

    public LiveData<List<Quiz>> getQuizzes(QuizzesResponseHandler handler) {
        return internetQuizRepository.fetch(handler);
    }

    interface QuizService {
        LiveData<List<Quiz>> fetch(QuizzesResponseHandler handler);
    }

    @WorkerThread
    public interface QuizzesResponseHandler {
        void updateQuizzes(@Nullable List<Quiz> quizzes);
    }

    static class InternetQuizRepository extends InternetRepository implements QuizService {

        private final InternetQuizService quizService;

        InternetQuizRepository() {
            quizService = retrofit.create(InternetQuizService.class);
        }

        @Override
        public LiveData<List<Quiz>> fetch(final QuizzesResponseHandler handler) {
            final MutableLiveData<List<Quiz>> liveData = new MutableLiveData<>();
            quizService.getQuizzes().enqueue(new Callback<List<Quiz>>() {
                @Override
                public void onResponse(Call<List<Quiz>> call, Response<List<Quiz>> response) {
                    handler.updateQuizzes(response.body());
                }

                public void onFailure(Call<List<Quiz>> call, Throwable t) {
                    handler.updateQuizzes(null);
                }
            });

            return liveData;
        }

        interface InternetQuizService {
            @GET("quizzes/")
            Call<List<Quiz>> getQuizzes();

            @GET("quizzes/{id}")
            Call<List<Quiz>> getQuizByID(@Path("id") UUID id);
        }

        private class QuizService {

        }
    }

    public static class LocalQuizRepository {
    }
}
