package me.srikavin.quiz.viewmodel;

import android.app.Application;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import me.srikavin.quiz.model.Quiz;
import me.srikavin.quiz.repository.QuizRepository;

public class QuizDetailViewModel extends AndroidViewModel {

    private final QuizRepository quizRepository;
    private Map<String, MutableLiveData<Quiz>> quizzes = new HashMap<>();

    public QuizDetailViewModel(@NonNull Application application) {
        super(application);
        quizRepository = new QuizRepository(application);
    }

    public LiveData<Quiz> getQuizByID(String id) {
        if (!quizzes.containsKey(id)) {
            quizzes.put(id, new MutableLiveData<>());
            updateQuiz(id);
        }
        return quizzes.get(id);
    }

    public void updateQuiz(final String id) {
        quizRepository.getQuizByID(id, new QuizRepository.QuizResponseHandler() {
            @Override
            public void handle(@Nullable Quiz newQuiz) {
                quizzes.get(id).postValue(newQuiz);
            }
        });
    }
}
