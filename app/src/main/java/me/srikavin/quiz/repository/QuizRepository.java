package me.srikavin.quiz.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.util.List;
import java.util.UUID;

import me.srikavin.quiz.model.Quiz;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;

public enum QuizRepository {
    INSTANCE;

    private InternetQuizRepository internetQuizRepository = new InternetQuizRepository();

    public LiveData<List<Quiz>> getQuizzes() {
        return internetQuizRepository.fetch();
    }

    interface QuizService {
        LiveData<List<Quiz>> fetch();
    }

    static class InternetQuizRepository extends InternetRepository implements QuizService {

        private final InternetQuizService quizService;

        InternetQuizRepository() {
            quizService = retrofit.create(InternetQuizService.class);
        }

        @Override
        public LiveData<List<Quiz>> fetch() {
            final MutableLiveData<List<Quiz>> liveData = new MutableLiveData<>();
            quizService.getQuizzes().enqueue(new Callback<List<Quiz>>() {
                @Override
                public void onResponse(Call<List<Quiz>> call, Response<List<Quiz>> response) {
                    liveData.postValue(response.body());
                }

                public void onFailure(Call<List<Quiz>> call, Throwable t) {
                    liveData.postValue(null);
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

    public static class WifiDirectQuizRepository {
    }
}
